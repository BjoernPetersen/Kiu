import 'package:kiu/bot/model.dart';

abstract class StateManager {
  BotState<PlayerState> get player;

  BotState<List<SongEntry>> get queueState;

  BotState<List<SongEntry>> get queueHistoryState;

  BotState<Volume> get volumeState;

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

abstract class ReadOnlyBotState<T> {
  Stream<T> get stream;

  T get lastValue;
}

abstract class BotState<T> extends ReadOnlyBotState<T> {
  void update(T value);
}
