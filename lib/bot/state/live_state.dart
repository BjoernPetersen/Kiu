import 'package:kiu/bot/model.dart';

abstract class LiveState {
  BotState<PlayerState?> get player;

  BotState<List<SongEntry>?> get queueState;

  BotState<List<SongEntry>?> get queueHistoryState;

  BotState<Volume?> get volumeState;

  BotState<List<NamedPlugin>?> get provider;

  BotState<List<NamedPlugin>?> get suggester;

  void reset() {
    player.update(null);
    queueState.update(null);
    queueHistoryState.update(null);
    volumeState.update(null);
    provider.update(null);
    suggester.update(null);
  }
}

abstract class ReadOnlyBotState<T> {
  Stream<T?> get stream;

  T? get lastValue;
}

abstract class BotState<T> extends ReadOnlyBotState<T> {
  void update(T? value);
}
