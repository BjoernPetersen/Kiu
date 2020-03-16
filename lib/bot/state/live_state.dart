import 'package:kiu/bot/model.dart';

abstract class LiveState {
  BotState<PlayerState> get player;

  BotState<List<SongEntry>> get queueState;

  BotState<List<SongEntry>> get queueHistoryState;

  BotState<Volume> get volumeState;
}

abstract class ReadOnlyBotState<T> {
  Stream<T> get stream;

  T get lastValue;
}

abstract class BotState<T> extends ReadOnlyBotState<T> {
  void update(T value);
}
