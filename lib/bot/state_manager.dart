import 'package:kiu/bot/model.dart';

abstract class StateManager {
  State<PlayerState> get player;

  State<List<SongEntry>> get queueState;

  State<List<SongEntry>> get queueHistoryState;

  @deprecated
  Stream<PlayerState> get playerState;

  @deprecated
  PlayerState get lastPlayerState;

  @deprecated
  Stream<List<SongEntry>> get queueHistory;

  @deprecated
  Stream<List<SongEntry>> get queue;

  @deprecated
  List<SongEntry> get lastQueueHistory;

  @deprecated
  List<SongEntry> get lastQueue;

  @deprecated
  void updateState(PlayerState state);

  @deprecated
  void updateQueue(List<SongEntry> queue);

  @deprecated
  void updateQueueHistory(List<SongEntry> history);

  @deprecated
  void close();
}

abstract class State<T> {
  Stream<T> get stream;

  T get lastValue;

  void update(T value);
}
