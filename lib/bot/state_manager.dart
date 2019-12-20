import 'package:kiu/bot/model.dart';

abstract class StateManager {
  Stream<PlayerState> get playerState;

  Stream<List<SongEntry>> get queue;

  void updateState(PlayerState state);

  void updateQueue(List<SongEntry> queue);

  void close();
}
