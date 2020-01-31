import 'package:kiu/bot/model.dart';

abstract class StateManager {
  Stream<PlayerState> get playerState;

  PlayerState get lastPlayerState;

  Stream<List<SongEntry>> get queueHistory;

  Stream<List<SongEntry>> get queue;

  List<SongEntry> get lastQueueHistory;

  List<SongEntry> get lastQueue;

  void updateState(PlayerState state);

  void updateQueue(List<SongEntry> queue);

  void updateQueueHistory(List<SongEntry> history);

  void close();
}
