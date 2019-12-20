import 'dart:async';

import 'package:dio/dio.dart';
import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state_manager.dart';

class StateManagerImpl implements StateManager {
  final _PeriodicChecker<PlayerState> _stateChecker;
  final _PeriodicChecker<List<SongEntry>> _queueChecker;

  StateManagerImpl(ConnectionManager connectionManager)
      : _stateChecker = _PeriodicChecker(
          connectionManager,
          (service) => service.getPlayerState(),
        ),
        _queueChecker = _PeriodicChecker(
          connectionManager,
          (service) => service.getQueue(),
        );

  @override
  Stream<PlayerState> get playerState => _stateChecker.stream;

  @override
  Stream<List<SongEntry>> get queue => _queueChecker.stream;

  @override
  void updateState(PlayerState state) {
    _stateChecker.update(state);
  }

  @override
  void updateQueue(List<SongEntry> queue) {
    _queueChecker.update(queue);
  }

  @override
  void close() {
    _stateChecker.close();
    _queueChecker.close();
  }
}

class _PeriodicChecker<T> {
  final Future<T> Function(BotService) call;
  final ConnectionManager connectionManager;
  final _state = StreamController<T>.broadcast();
  T _lastValue;
  Timer _timer;
  Future<void> _job;

  _PeriodicChecker(this.connectionManager, this.call) {
    _state.onListen = _onListenerChange;
    _state.onCancel = _onListenerChange;
  }

  _start() {
    _timer = Timer.periodic(Duration(seconds: 2), (_) {
      if (_job == null) {
        _job = check();
      }
    });
  }

  _stop() {
    _timer.cancel();
    _job = null;
  }

  _onListenerChange() {
    final hasListener = _state.hasListener;
    if (hasListener && _lastValue != null) {
      update(_lastValue);
    }

    if (hasListener && _timer == null) {
      _start();
    } else if (!hasListener && _timer != null) {
      _stop();
    }
  }

  Future<void> check() async {
    // TODO this might fail
    final service = await connectionManager.getService();
    try {
      final result = await call(service)
          .timeout(Duration(seconds: 5), onTimeout: () => null);
      update(result);
    } on DioError catch (e) {
      if (e.type != DioErrorType.RESPONSE || e.response.statusCode == 401) {
        connectionManager.reset();
      }
    } catch (err) {
      print("Unknown error: $err");
    } finally {
      _job = null;
    }
  }

  Stream<T> get stream => _state.stream;

  void update(T value) {
    _state.add(value);
    _lastValue = value;
  }

  void close() {
    _state.close();
  }
}
