import 'dart:async';

import 'package:dio/dio.dart';
import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state/state_manager.dart';

class StateManagerImpl implements StateManager {
  final _PeriodicChecker<PlayerState> _playerState;
  final _PeriodicChecker<List<SongEntry>> _queueState;
  final _PeriodicChecker<List<SongEntry>> _queueHistoryState;
  final _PeriodicChecker<Volume> _volumeState;

  StateManagerImpl(ConnectionManager connectionManager)
      : _playerState = _PeriodicChecker(
          connectionManager,
          (service) => service.getPlayerState(),
        ),
        _queueState = _PeriodicChecker(
          connectionManager,
          (service) => service.getQueue(),
        ),
        _queueHistoryState = _PeriodicChecker(
          connectionManager,
          (service) => service.getQueueHistory(),
        ),
        _volumeState = _PeriodicChecker(
          connectionManager,
          (service) => service.getVolume(),
        );

  @override
  BotState<PlayerState> get player => _playerState;

  @override
  BotState<List<SongEntry>> get queueState => _queueState;

  @override
  BotState<List<SongEntry>> get queueHistoryState => _queueHistoryState;

  @override
  BotState<Volume> get volumeState => _volumeState;

  @override
  Stream<PlayerState> get playerState => player.stream;

  @override
  PlayerState get lastPlayerState => player.lastValue;

  @override
  Stream<List<SongEntry>> get queue => queueState.stream;

  @override
  List<SongEntry> get lastQueue => queueState.lastValue;

  @override
  Stream<List<SongEntry>> get queueHistory => queueHistoryState.stream;

  @override
  List<SongEntry> get lastQueueHistory => queueHistoryState.lastValue;

  @override
  void updateState(PlayerState state) {
    player.update(state);
  }

  @override
  void updateQueue(List<SongEntry> queue) {
    queueState.update(queue);
  }

  @override
  void updateQueueHistory(List<SongEntry> history) {
    queueState.update(history);
  }

  @override
  void close() {
    _playerState.close();
    _queueState.close();
    _queueHistoryState.close();
  }
}

class _PeriodicChecker<T> implements BotState<T> {
  final Future<T> Function(BotService) call;
  final ConnectionManager connectionManager;
  final _state = StreamController<T>.broadcast();
  @override
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
