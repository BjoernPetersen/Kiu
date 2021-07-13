import 'package:flutter/material.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/view/widget/album_art.dart';
import 'package:kiu/view/widget/duration_text.dart';

class SongTile extends StatelessWidget {
  final Song song;
  final String? username;
  final Widget? trailing;
  final void Function()? onPressed;
  final bool enabled;
  final String? tooltip;

  const SongTile(
    this.song, {
    this.username,
    this.trailing,
    this.onPressed,
    this.enabled = true,
    this.tooltip,
  }) : super();

  Widget _buildTile(BuildContext context) => ListTile(
        enabled: enabled,
        leading: SizedBox(
          width: 50,
          height: 50,
          child: AlbumArt(song),
        ),
        title: Text(
          song.title,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
        subtitle: Text(
          song.description,
          maxLines: 1,
          overflow: TextOverflow.ellipsis,
        ),
        contentPadding: trailing == null
            ? const EdgeInsets.symmetric(horizontal: 8)
            : const EdgeInsets.only(left: 8),
        dense: true,
        trailing: _createTrailing(context),
        onTap: onPressed,
      );

  @override
  Widget build(BuildContext context) {
    final tooltip = this.tooltip;
    if (tooltip == null) {
      return _buildTile(context);
    } else {
      return Tooltip(
        message: tooltip,
        child: _buildTile(context),
      );
    }
  }

  Widget _createRightInfo(BuildContext context) {
    final children = <Widget>[
      DurationText(song.duration),
    ];

    final username = this.username;
    if (username != null) {
      children.add(SizedBox(
        height: 10,
      ));
      children.add(Text(
        username,
        overflow: TextOverflow.ellipsis,
      ));
    }

    return DefaultTextStyle(
      style: TextStyle(
        color: Colors.black45,
        fontSize: 12,
      ),
      child: Container(
        constraints: BoxConstraints(maxWidth: 80),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.end,
          mainAxisSize: MainAxisSize.min,
          children: children,
        ),
      ),
    );
  }

  Widget _createTrailing(BuildContext context) {
    final children = <Widget>[_createRightInfo(context)];
    final trailing = this.trailing;
    if (trailing != null) {
      children.add(trailing);
    }

    return Row(
      mainAxisAlignment: MainAxisAlignment.end,
      mainAxisSize: MainAxisSize.min,
      children: children,
    );
  }
}
