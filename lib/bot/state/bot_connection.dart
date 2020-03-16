import 'dart:async';

import 'package:kiu/bot/bot.dart';
import 'package:kiu/bot/state/state_manager.dart';

class BotConnection {
  final _errors = _EventCache(retainDuration: Duration(seconds: 10));
  final _successes = _EventCache(retainDuration: Duration(seconds: 10));
  final BotState<Bot> _bot = _ManualState();
  final BotState<BotConnectionState> _state = _ManualState();

  ReadOnlyBotState<Bot> get bot => _bot;

  ReadOnlyBotState<BotConnectionState> get state => _state;

  BotConnection() {
    _errors.onChange = (_) => _updateState();
  }

  reportSuccess() {
    _successes.add();
  }

  reportError(dynamic error) {
    _errors.add();
  }

  setBot(Bot bot) {
    _errors.clear();
    _successes.clear();
    _bot.update(bot);
    _updateState();
  }

  BotConnectionState _determineState() {
    if (_bot.lastValue == null) {
      return BotConnectionState.none;
    }
    final errors = _errors.count;
    final successes = _successes.count;
    if (errors == 0 && successes > 0) {
      return BotConnectionState.alive;
    } else if (errors < 5) {
      return BotConnectionState.questionable;
    } else {
      return BotConnectionState.dead;
    }
  }

  _updateState() {
    _state.update(_determineState());
  }
}

class _EventCache {
  final Duration _retainDuration;

  final _events = <DateTime>[];
  Function(int errorCount) onChange = (_) {};

  _EventCache({
    Duration retainDuration = const Duration(seconds: 10),
  }) : this._retainDuration = retainDuration;

  int get count => _events.length;

  add() {
    final time = DateTime.now();
    _events.add(time);
    _cleanup(time);
    onChange(_events.length);
  }

  clear() {
    _events.clear();
  }

  Future<void> _cleanup(DateTime time) async {
    await Future.delayed(_retainDuration);
    _events.remove(time);
  }
}

enum BotConnectionState {
  alive,
  questionable,
  dead,
  none,
}

class _ManualState<T> implements BotState<T> {
  final _state = StreamController<T>.broadcast();
  @override
  T lastValue;

  @override
  Stream<T> get stream => _state.stream;

  @override
  void update(T value) {
    _state.add(value);
    lastValue = value;
  }

  void close() {
    _state.close();
  }
}
