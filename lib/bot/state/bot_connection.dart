import 'dart:async';

import 'package:kiu/bot/bot.dart';
import 'package:kiu/bot/state/live_state.dart';
import 'package:kiu/data/preferences.dart';

const ERROR_RETENTION = const Duration(seconds: 10);
const SUCCESS_RETENTION = const Duration(seconds: 10);

const ALIVE_SUCCESS_MIN = 5;
const ALIVE_ERROR_MAX = 0;
const QUESTIONABLE_ERROR_MAX = 5;

class BotConnection {
  final _errors = _EventCache(retainDuration: ERROR_RETENTION);
  final _successes = _EventCache(retainDuration: SUCCESS_RETENTION);
  final BotState<Bot> _bot = _ManualState();
  final BotState<BotConnectionState> _state = _ManualState();

  ReadOnlyBotState<Bot> get bot => _bot;

  ReadOnlyBotState<BotConnectionState> get state => _state;

  factory BotConnection.load() {
    final bot = _loadBot();
    return BotConnection._(bot);
  }

  BotConnection._(Bot bot) {
    _bot.update(bot);
    _errors.onChange = (_) => _updateState();
    _successes.onChange = (_) => _updateState();
    _bot.stream.listen((event) {
      _saveBot(event);
      _updateState();
    });
    _updateState();
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
    if (errors <= ALIVE_ERROR_MAX && successes > ALIVE_SUCCESS_MIN) {
      return BotConnectionState.alive;
    } else if (errors < QUESTIONABLE_ERROR_MAX) {
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
    onChange(_events.length);
  }

  @override
  String toString() {
    return _events.toString();
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

Bot _loadBot() {
  final ip = Preference.bot_ip.getString();
  if (ip == null) {
    return null;
  } else {
    return Bot(ip: ip);
  }
}

Future<void> _saveBot(Bot bot) async {
  if (bot == null) {
    await Preference.bot_ip.remove();
  } else {
    await Preference.bot_ip.setString(bot.ip);
  }
}
