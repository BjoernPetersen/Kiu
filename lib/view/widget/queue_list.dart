import 'dart:async';

import 'package:flutter/material.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/bot/state_manager.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/widget/offset_fill_sliver.dart';
import 'package:kiu/view/widget/queue_card.dart';
import 'package:kiu/view/widget/song_tile.dart';
import 'package:reorderables/reorderables.dart';

const CARD_HEIGHT = 72.0;

class QueueList extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _QueueListState();
}

class _QueueListState extends State<QueueList> {
  List<SongEntry> _history = [];
  List<SongEntry> _queue = [];
  final ConnectionManager connectionManager = service<ConnectionManager>();
  Function _tokenListener;
  StreamSubscription _historySubscription;
  StreamSubscription _queueSubscription;

  @override
  void initState() {
    super.initState();
    _tokenListener = () => this.setState(() {});
    connectionManager.addTokenListener(_tokenListener);
    final manager = service<StateManager>();
    _history = manager.lastQueueHistory ?? [];
    _historySubscription = manager.queueHistory.listen(_onHistoryChange);
    _queue = manager.lastQueue ?? [];
    _queueSubscription = manager.queue.listen(_onQueueChange);
  }

  @override
  void dispose() {
    connectionManager.removeTokenListener(_tokenListener);
    _historySubscription.cancel();
    _queueSubscription.cancel();
    super.dispose();
  }

  _onQueueChange(List<SongEntry> queue) {
    if (_queue != queue)
      setState(() {
        _queue = queue;
      });
  }

  _onHistoryChange(List<SongEntry> history) {
    if (_history != history)
      setState(() {
        _history = history;
      });
  }

  Widget _buildQueueItem(BuildContext context, int index) {
    final entry = _queue[index];
    return QueueCard(entry);
  }

  Future<void> _enqueue(Song song) async {
    if (_queue.map((it) => it.song).contains(song)) {
      return;
    }
    setState(() {
      _queue.add(SongEntry(
        song: song,
        userName: Preference.username.getString(),
      ));
    });
    final bot = await service<ConnectionManager>().getService();
    try {
      final queue = await bot.enqueue(song.id, song.provider.id);
      service<StateManager>().updateQueue(queue);
    } catch (e) {
      print(e);
    }
  }

  Widget _buildHistoryItem(BuildContext context, int index) {
    final entry = _history[index];
    return Opacity(
      opacity: 0.5,
      child: Card(
        child: SongTile(
          entry.song,
          tooltip: "Tap to enqueue",
          enabled: true,
          onPressed: () => _enqueue(entry.song),
        ),
      ),
    );
  }

  Future _onReorder(int oldIndex, int newIndex) async {
    final entry = _queue[oldIndex];
    final song = entry.song;

    // put a manual update before actually doing it
    final tempQueue = _queue.toList();
    tempQueue.removeAt(oldIndex);
    tempQueue.insert(newIndex, entry);
    service<StateManager>().updateQueue(tempQueue);

    final bot = await connectionManager.getService();
    final newQueue = await bot.moveEntry(newIndex, song.id, song.provider.id);
    service<StateManager>().updateQueue(newQueue);
  }

  Widget _buildQueueList() {
    if (connectionManager.hasPermission(Permission.MOVE)) {
      return ReorderableSliverList(
        delegate: ReorderableSliverChildBuilderDelegate(
          _buildQueueItem,
          semanticIndexOffset: _history.length,
          childCount: _queue.length,
        ),
        onReorder: _onReorder,
      );
    } else {
      return SliverList(
        delegate: SliverChildBuilderDelegate(
          _buildQueueItem,
          semanticIndexOffset: _history.length,
          childCount: _queue.length,
        ),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    return CustomScrollView(
      controller: ScrollController(
        initialScrollOffset: CARD_HEIGHT * _history.length,
      ),
      slivers: <Widget>[
        SliverList(
          delegate: SliverChildBuilderDelegate(
            _buildHistoryItem,
            childCount: _history.length,
          ),
        ),
        _buildQueueList(),
        OffsetFillSliver(offset: CARD_HEIGHT),
      ],
    );
  }
}
