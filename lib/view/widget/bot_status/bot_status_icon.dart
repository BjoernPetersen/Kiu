import 'dart:async';

import 'package:kiu/bot/state/bot_connection.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';

class BotStatusIcon extends StatefulWidget {
  @override
  _BotStatusIconState createState() => _BotStatusIconState();
}

class _BotStatusIconState extends State<BotStatusIcon> {
  BotConnectionState _state;
  StreamSubscription<BotConnectionState> _sub;

  @override
  void initState() {
    super.initState();
    final botConnection = service<BotConnection>();
    final state = botConnection.state;
    _sub = state.stream.listen((event) => setState(() {
          _state = event;
        }));
    _state = state.lastValue;
  }

  @override
  void dispose() {
    _sub.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    switch (_state) {
      case BotConnectionState.alive:
        return Icon(
          Icons.signal_cellular_4_bar,
        );
      case BotConnectionState.questionable:
        return Icon(
          Icons.signal_cellular_connected_no_internet_4_bar,
        );
      case BotConnectionState.dead:
        return Icon(
          Icons.signal_cellular_null,
        );
      case BotConnectionState.none:
      default:
        return Icon(
          Icons.signal_cellular_off,
        );
    }
  }
}
