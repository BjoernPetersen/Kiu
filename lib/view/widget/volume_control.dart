import 'dart:async';

import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/bot/state/live_state.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';

class VolumeControl extends StatefulWidget {
  @override
  _VolumeControlState createState() => _VolumeControlState();
}

class _VolumeControlState extends State<VolumeControl> {
  late Volume _volume;
  late StreamSubscription _sub;

  @override
  void initState() {
    super.initState();
    final state = service<LiveState>().volumeState;
    this._volume = state.lastValue ?? Volume.unsupported();
    this._sub = state.stream.listen((value) {
      setState(() {
        _volume = value ?? Volume.unsupported();
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

  bool _canChange([AccessManager? connectionManager]) {
    final manager = connectionManager ?? service<AccessManager>();
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
    final manager = service<AccessManager>();
    if (!_canChange(manager)) return;
    final bot = await manager.createService();
    try {
      bot.setVolume(volume);
      service<LiveState>().volumeState.update(Volume.supported(volume));
    } catch (err) {
      ScaffoldMessenger.of(context).showSnackBar(SnackBar(
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
