import 'dart:async';

import 'package:dio/dio.dart';
import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state/live_state.dart';
import 'package:kiu/bot/state/login_error_state.dart';
import 'package:kiu/data/dependency_model.dart';

class LiveStateImpl extends LiveState {
  final _PeriodicChecker<PlayerState> _playerState;
  final _PeriodicChecker<List<SongEntry>> _queueState;
  final _PeriodicChecker<List<SongEntry>> _queueHistoryState;
  final _PeriodicChecker<Volume> _volumeState;
  final _PeriodicChecker<List<NamedPlugin>> _providerState;
  final _PeriodicChecker<List<NamedPlugin>> _suggesterState;

  LiveStateImpl(AccessManager accessManager)
      : _playerState = _PeriodicChecker(
          accessManager,
          (service) => service.getPlayerState(),
        ),
        _queueState = _PeriodicChecker(
          accessManager,
          (service) => service.getQueue(),
        ),
        _queueHistoryState = _PeriodicChecker(
          accessManager,
          (service) => service.getQueueHistory(),
        ),
        _volumeState = _PeriodicChecker(
          accessManager,
          (service) => service.getVolume(),
        ),
        _providerState = _PeriodicChecker(
          accessManager,
          (service) => service.getProviders(),
          Duration(seconds: 10),
        ),
        _suggesterState = _PeriodicChecker(
          accessManager,
          (service) => service.getSuggesters(),
          Duration(seconds: 10),
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
  BotState<List<NamedPlugin>> get provider => _providerState;

  @override
  BotState<List<NamedPlugin>> get suggester => _suggesterState;
}

class _PeriodicChecker<T> implements BotState<T> {
  final Future<T> Function(BotService) call;
  final AccessManager accessManager;
  final _state = StreamController<T>.broadcast();
  @override
  T lastValue;
  final Duration _interval;
  Timer _timer;
  Future<void> _job;

  _PeriodicChecker(
    this.accessManager,
    this.call, [
    this._interval = const Duration(seconds: 1),
  ]) {
    _state.onListen = _onListenerChange;
    _state.onCancel = _onListenerChange;
  }

  _start() {
    _job = check();
    _timer = Timer.periodic(_interval, (_) {
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
      final service = await accessManager.createService();
      if (service == null) {
        return;
      }
      final result = await call(service)
          .timeout(Duration(seconds: 5), onTimeout: () => null);
      update(result);
    } on RefreshTokenException catch (e) {
      service<LoginErrorState>().update(e);
    } on DioError catch (e) {
      if (e.type != DioErrorType.RESPONSE || e.response.statusCode == 401) {
        accessManager.reset();
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
