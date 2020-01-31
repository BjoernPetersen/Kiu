import 'dart:async';

import 'package:dio/dio.dart';
import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state_manager.dart';

class StateManagerImpl implements StateManager {
  final _PeriodicChecker<PlayerState> _stateChecker;
  final _PeriodicChecker<List<SongEntry>> _queueChecker;
  final _PeriodicChecker<List<SongEntry>> _queueHistoryChecker;

  StateManagerImpl(ConnectionManager connectionManager)
      : _stateChecker = _PeriodicChecker(
          connectionManager,
          (service) => service.getPlayerState(),
        ),
        _queueChecker = _PeriodicChecker(
          connectionManager,
          (service) => service.getQueue(),
        ),
        _queueHistoryChecker = _PeriodicChecker(
          connectionManager,
          (service) => service.getQueueHistory(),
        );

  @override
  Stream<PlayerState> get playerState => _stateChecker.stream;

  @override
  PlayerState get lastPlayerState => _stateChecker.lastValue;

  @override
  Stream<List<SongEntry>> get queue => _queueChecker.stream;

  @override
  List<SongEntry> get lastQueue => _queueChecker.lastValue;

  @override
  Stream<List<SongEntry>> get queueHistory => _queueHistoryChecker.stream;

  @override
  List<SongEntry> get lastQueueHistory => _queueHistoryChecker.lastValue;

  @override
  void updateState(PlayerState state) {
    _stateChecker.update(state);
  }

  @override
  void updateQueue(List<SongEntry> queue) {
    _queueChecker.update(queue);
  }

  @override
  void updateQueueHistory(List<SongEntry> history) {
    _queueChecker.update(history);
  }

  @override
  void close() {
    _stateChecker.close();
    _queueChecker.close();
    _queueHistoryChecker.close();
  }
}

class _PeriodicChecker<T> {
  final Future<T> Function(BotService) call;
  final ConnectionManager connectionManager;
  final _state = StreamController<T>.broadcast();
  T lastValue;
  Timer _timer;
  Future<void> _job;

  _PeriodicChecker(this.connectionManager, this.call) {
    _state.onListen = _onListenerChange;
    _state.onCancel = _onListenerChange;
  }

  _start() {
    _timer = Timer.periodic(Duration(seconds: 1), (_) {
      if (_job == null) {
        _job = check();
      }
    });
  }

  _stop() {
    _timer.cancel();
    _timer = null;
    _job = null;
  }

  _onListenerChange() {
    final hasListener = _state.hasListener;
    if (hasListener && _timer == null) {
      _start();
    } else if (!hasListener && _timer != null) {
      _stop();
    }
  }

  Future<void> check() async {
    try {
      final service = await connectionManager.getService();
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
    lastValue = value;
  }

  void close() {
    _state.close();
  }
}
