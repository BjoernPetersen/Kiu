import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/bot/state_manager.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/widget/song_tile.dart';

class QueueCard extends StatelessWidget {
  final SongEntry songEntry;
  final ConnectionManager connectionManager;

  QueueCard(this.songEntry) : connectionManager = service<ConnectionManager>();

  Future<void> _delete() async {
    final song = songEntry.song;
    try {
      final bot = await connectionManager.getService();
      final queue = await bot.dequeue(song.id, song.provider.id);
      service<StateManager>().updateQueue(queue);
    } catch (e) {
      print(e);
    }
  }

  Widget _createTrailing() {
    if (connectionManager.hasPermission(Permission.SKIP)) {
      return IconButton(
        icon: Icon(Icons.delete),
        onPressed: _delete,
      );
    } else {
      return null;
    }
  }

  @override
  Widget build(BuildContext context) => Card(
        child: SongTile(
          songEntry.song,
          username: songEntry.userName,
          trailing: _createTrailing(),
        ),
      );
}
