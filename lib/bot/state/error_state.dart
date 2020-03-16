import 'dart:async';

import 'package:kiu/bot/state/state_manager.dart';
import 'package:kiu/data/action_error.dart';

class ErrorState implements BotState<ActionError> {
  final _controller = StreamController<ActionError>.broadcast();
  ActionError _lastValue;

  @override
  ActionError get lastValue => _lastValue;

  @override
  Stream<ActionError> get stream => _controller.stream;

  @override
  void update(ActionError value) {
    _controller.add(value);
    _lastValue = value;
    if (value != null) _cleanup(value);
  }
  
  Future<void> _cleanup(ActionError value) async {
    await Future.delayed(Duration(seconds: 2));
    if (_lastValue == value) {
      update(null);
    }
  }

  close() {
    _controller.close();
  }
}
