import 'dart:async';

import 'package:kiu/bot/state/live_state.dart';
import 'package:kiu/view/common.dart';

class BotStateBuilder<T> extends StatefulWidget {
  final Widget Function(BuildContext, T) builder;
  final BotState<T> state;

  const BotStateBuilder({Key key, this.builder, this.state}) : super(key: key);

  @override
  _BotStateBuilderState<T> createState() => _BotStateBuilderState<T>();
}

class _BotStateBuilderState<T> extends State<BotStateBuilder<T>> {
  StreamSubscription<T> _sub;
  T _value;

  @override
  void initState() {
    super.initState();
    _sub = widget.state.stream.listen((event) {
      setState(() {
        _value = event;
      });
    });
    _value = widget.state.lastValue;
  }

  @override
  void dispose() {
    _sub.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return widget.builder(context, _value);
  }
}
