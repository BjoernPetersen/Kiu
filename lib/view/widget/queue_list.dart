import 'dart:async';

import 'package:flutter/material.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/bot/state_manager.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/widget/queue_card.dart';
import 'package:reorderables/reorderables.dart';

class QueueList extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _QueueListState();
}

class _QueueListState extends State<QueueList> {
  List<SongEntry> _queue = [];
  final ConnectionManager connectionManager = service<ConnectionManager>();
  Function _tokenListener;
  StreamSubscription _queueSubscription;

  @override
  void initState() {
    super.initState();
    _tokenListener = () => this.setState(() {});
    final manager = service<StateManager>();
    _queue = manager.lastQueue ?? [];
    _queueSubscription = manager.queue.listen(_onQueueChange);
  }

  @override
  void dispose() {
    connectionManager.removeTokenListener(_tokenListener);
    _queueSubscription.cancel();
    super.dispose();
  }

  _onQueueChange(List<SongEntry> queue) {
    setState(() {
      _queue = queue;
    });
  }

  Widget _buildQueueItem(BuildContext context, int index) {
    final entry = _queue[index];
    return QueueCard(entry);
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

  @override
  Widget build(BuildContext context) {
    if (connectionManager.hasPermission(Permission.MOVE)) {
      return CustomScrollView(
        controller: PrimaryScrollController.of(context),
        slivers: <Widget>[
          ReorderableSliverList(
            delegate: ReorderableSliverChildBuilderDelegate(
              _buildQueueItem,
              childCount: _queue.length,
            ),
            onReorder: _onReorder,
          )
        ],
      );
    } else {
      return ListView.builder(
        itemBuilder: _buildQueueItem,
        itemCount: _queue.length,
      );
    }
  }
}
