import 'dart:async';

import 'package:expandable/expandable.dart';
import 'package:flutter/material.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/bot/state_manager.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/widget/song_tile.dart';

class PlayerControl extends StatefulWidget {
  @override
  State<StatefulWidget> createState() => _PlayerControlState();
}

class _PlayerControlState extends State<PlayerControl> {
  PlayerState _state;
  StreamSubscription sub;

  @override
  void initState() {
    super.initState();
    final manager = service<StateManager>();
    sub = manager.playerState.listen(_onStateChange);
    _state = manager.lastPlayerState;
  }

  @override
  void dispose() {
    sub.cancel();
    super.dispose();
  }

  void _onStateChange(PlayerState state) {
    setState(() {
      _state = state;
    });
  }

  @override
  Widget build(BuildContext context) {
    final state = _state;
    if (state == null) {
      return Padding(
        padding: const EdgeInsets.symmetric(vertical: 10),
        child: Center(child: Text('No data')),
      );
    }
    final type = state.state;
    switch (type) {
      case PlayerStateType.play:
      case PlayerStateType.pause:
        return _createControl(_songInfo(state.songEntry), state);
      case PlayerStateType.stop:
      case PlayerStateType.error:
        return _createControl(Center(child: Text('Player is stopped')), state);
    }
  }

  Widget _songInfo(SongEntry entry) {
    return SongTile(
      entry.song,
      username: entry.userName,
    );
  }

  Future<void> _changeState(PlayerStateChangeAction action) async {
    final bot = await service<ConnectionManager>().getService();
    await bot.changePlayerState(PlayerStateChange(action));
  }

  Widget _createControls(PlayerStateType type) {
    List<Widget> children = [];
    final cm = service<ConnectionManager>();
    if (cm.hasPermission(Permission.PAUSE)) {
      IconData icon;
      PlayerStateChangeAction action;
      if (type == PlayerStateType.play) {
        icon = Icons.pause;
        action = PlayerStateChangeAction.pause;
      } else {
        icon = Icons.play_arrow;
        action = PlayerStateChangeAction.play;
      }
      children.add(IconButton(
        icon: Icon(icon),
        onPressed: () => _changeState(action),
      ));
    }

    if (cm.hasPermission(Permission.SKIP)) {
      children.add(IconButton(
        icon: Icon(Icons.skip_next),
        onPressed: () => _changeState(PlayerStateChangeAction.skip),
      ));
    }

    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
      children:
          children.map((it) => Expanded(child: it)).toList(growable: false),
    );
  }

  Widget _createControl(Widget songPart, PlayerState state) {
    double progress = 0.0;
    final song = state.songEntry?.song;
    if (state.progress != null && song?.duration != null) {
      progress = state.progress / song.duration;
    }
    return Column(
      mainAxisSize: MainAxisSize.min,
      children: <Widget>[
        ExpandablePanel(
          header: songPart,
          expanded: _createControls(state.state),
        ),
        LinearProgressIndicator(value: progress)
      ],
    );
  }
}
