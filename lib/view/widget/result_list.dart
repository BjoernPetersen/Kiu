import 'dart:async';

import 'package:flutter/material.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state/state_manager.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/widget/song_tile.dart';

class ResultList extends StatefulWidget {
  final List<Song> results;
  final Widget Function(BuildContext context, Song song) trailingBuilder;

  const ResultList({@required this.results, this.trailingBuilder}) : super();

  @override
  State<StatefulWidget> createState() => _ResultListState(
        results,
        trailingBuilder,
      );
}

class _ResultListState extends State<ResultList> {
  final List<Song> results;
  final Widget Function(BuildContext context, Song song) trailingBuilder;
  Set<Song> queue = {};
  StreamSubscription queueSubscription;

  _ResultListState(this.results, this.trailingBuilder);

  @override
  initState() {
    super.initState();
    final manager = service<StateManager>();
    final lastQueue = manager.lastQueue;
    if (lastQueue != null) {
      queue = lastQueue.map((it) => it.song).toSet();
    }
    queueSubscription = manager.queue.listen((queue) {
      setState(() {
        this.queue = queue.map((it) => it.song).toSet();
      });
    });
  }

  @override
  void dispose() {
    queueSubscription.cancel();
    super.dispose();
  }

  Future<void> _addSong(Song song) async {
    setState(() {
      this.queue.add(song);
    });
    final bot = await service<ConnectionManager>().getService();
    try {
      final queue = await bot.enqueue(song.id, song.provider.id);
      service<StateManager>().updateQueue(queue);
    } catch (e) {
      print(e);
    }
  }

  @override
  Widget build(BuildContext context) {
    return ListView.builder(
      itemCount: results.length,
      itemBuilder: (_, index) {
        final song = results[index];
        return Card(
          child: SongTile(
            song,
            enabled: !queue.contains(song),
            onPressed: () => _addSong(song),
            trailing:
                trailingBuilder == null ? null : trailingBuilder(context, song),
          ),
        );
      },
    );
  }
}
