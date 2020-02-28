import 'dart:async';

import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/bot/state_manager.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';

class VolumeControl extends StatefulWidget {
  @override
  _VolumeControlState createState() => _VolumeControlState();
}

class _VolumeControlState extends State<VolumeControl> {
  Volume _volume;
  StreamSubscription _sub;

  @override
  void initState() {
    super.initState();
    final state = service<StateManager>().volumeState;
    this._volume = state.lastValue ?? Volume.unsupported();
    this._sub = state.stream.listen((value) {
      setState(() {
        _volume = value;
      });
    });
  }

  @override
  void dispose() {
    _sub.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (_volume.isSupported) {
      return Row(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          VerticalDivider(),
          Column(
            children: <Widget>[
              Padding(
                padding: const EdgeInsets.only(top: 10.0),
                child: Text(context.messages.state.volume),
              ),
              Expanded(
                child: RotatedBox(
                  quarterTurns: 3,
                  child: Slider(
                    value: _volume.volume / 100,
                    onChangeStart: _onVolumeChangeStart,
                    onChangeEnd: _onVolumeChangeEnd,
                    onChanged: _onVolumeChange,
                  ),
                ),
              ),
            ],
          ),
        ],
      );
    } else {
      return Container();
    }
  }

  bool _canChange([ConnectionManager connectionManager]) {
    final manager = connectionManager ?? service<ConnectionManager>();
    return manager.hasPermission(Permission.CHANGE_VOLUME);
  }

  void _onVolumeChange(double value) {
    if (!_canChange()) return;
    setState(() {
      this._volume = Volume.supported((value * 100).round());
    });
  }

  void _onVolumeChangeStart(double value) {
    _sub.pause();
  }

  Future<void> _setVolume(int volume) async {
    final manager = service<ConnectionManager>();
    if (!_canChange(manager)) return;
    final bot = await manager.getService();
    try {
      bot.setVolume(volume);
      service<StateManager>().volumeState.update(Volume.supported(volume));
    } catch (err) {
      Scaffold.of(context).showSnackBar(SnackBar(
        content: Text("Could not set volume: $err"),
        duration: Duration(seconds: 5),
        action: SnackBarAction(
          label: "Try again",
          onPressed: () => _setVolume(volume),
        ),
      ));
    }
  }

  void _onVolumeChangeEnd(double value) {
    _sub.resume();
    _setVolume((value * 100).round());
  }
}
