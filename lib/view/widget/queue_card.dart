import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/view/widget/song_tile.dart';

class QueueCard extends StatelessWidget {
  final SongEntry songEntry;

  const QueueCard(this.songEntry) : super();

  @override
  Widget build(BuildContext context) => Card(
        child: SongTile(
          songEntry.song,
          username: songEntry.userName,
        ),
      );
}
