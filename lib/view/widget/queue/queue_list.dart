import 'dart:async';

import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/bot/state/live_state.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/empty_state.dart';
import 'package:kiu/view/widget/offset_fill_sliver.dart';
import 'package:kiu/view/widget/queue/queue_card.dart';
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
  final AccessManager accessManager = service<AccessManager>();
  late Function() _tokenListener;
  late StreamSubscription _historySubscription;
  late StreamSubscription _queueSubscription;

  @override
  void initState() {
    super.initState();
    _tokenListener = () => this.setState(() {});
    accessManager.addListener(_tokenListener);
    final manager = service<LiveState>();
    _history = manager.queueHistoryState.lastValue ?? [];
    _historySubscription =
        manager.queueHistoryState.stream.listen(_onHistoryChange);
    _queue = manager.queueState.lastValue ?? [];
    _queueSubscription = manager.queueState.stream.listen(_onQueueChange);
  }

  @override
  void dispose() {
    accessManager.removeListener(_tokenListener);
    _historySubscription.cancel();
    _queueSubscription.cancel();
    super.dispose();
  }

  _onQueueChange(List<SongEntry>? queue) {
    if (_queue != queue)
      setState(() {
        _queue = queue ?? [];
      });
  }

  _onHistoryChange(List<SongEntry>? history) {
    if (_history != history)
      setState(() {
        _history = history ?? [];
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
    final bot = await service<AccessManager>().createService();
    try {
      final queue = await bot.enqueue(song.id, song.provider.id);
      service<LiveState>().queueState.update(queue);
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
          username: entry.userName,
          tooltip: context.messages.queue.tapToEnqueue,
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
    service<LiveState>().queueState.update(tempQueue);

    final bot = await accessManager.createService();
    final newQueue = await bot.moveEntry(newIndex, song.id, song.provider.id);
    service<LiveState>().queueState.update(newQueue);
  }

  Widget _buildQueueList() {
    if (_queue.isEmpty) {
      return SliverFillViewport(
        delegate: SliverChildBuilderDelegate(
            (context, index) => EmptyState(text: context.messages.queue.empty),
            childCount: 1),
      );
    } else if (accessManager.hasPermission(Permission.MOVE)) {
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

  List<Widget> _createSlivers() {
    final List<Widget> result = [
      SliverList(
        delegate: SliverChildBuilderDelegate(
          _buildHistoryItem,
          childCount: _history.length,
        ),
      ),
      _buildQueueList(),
    ];
    if (_queue.isNotEmpty) result.add(OffsetFillSliver(offset: CARD_HEIGHT));
    return result;
  }

  @override
  Widget build(BuildContext context) {
    return CustomScrollView(
      controller: ScrollController(
        keepScrollOffset: false,
        initialScrollOffset: CARD_HEIGHT * _history.length,
      ),
      slivers: _createSlivers(),
    );
  }
}
