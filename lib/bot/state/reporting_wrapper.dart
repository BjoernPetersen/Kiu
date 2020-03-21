import 'package:dio/dio.dart';
import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state/bot_connection.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:retrofit/http.dart';

class ReportingWrapper implements BotService {
  final BotService delegate;

  ReportingWrapper(this.delegate);

  Future<T> _report<T>(Future<T> call) async {
    final botConnection = service<BotConnection>();
    try {
      final result = await call;
      botConnection.reportSuccess();
      return result;
    } on DioError catch (e) {
      if (e.type == DioErrorType.RESPONSE) {
        final code = e.response.statusCode;
        if (code >= 500 && code < 600) {
          botConnection.reportError(e);
        }
      } else if (e.type != DioErrorType.CANCEL) {
        botConnection.reportError(e);
      }
      throw e;
    }
  }

  @override
  Future<Tokens> login(String basic) {
    return _report(delegate.login(basic));
  }

  @override
  Future<BotInfo> getVersion() {
    return _report(delegate.getVersion());
  }

  @override
  Future<Volume> setVolume(int volume) {
    return _report(delegate.setVolume(volume));
  }

  @override
  Future<Volume> getVolume() {
    return _report(delegate.getVolume());
  }

  @override
  Future<PlayerState> changePlayerState(PlayerStateChange change) {
    return _report(delegate.changePlayerState(change));
  }

  @override
  Future<List<SongEntry>> dequeue(String songId, String providerId) {
    return _report(delegate.dequeue(songId, providerId));
  }

  @override
  Future<List<SongEntry>> enqueue(String songId, String providerId) {
    return _report(delegate.enqueue(songId, providerId));
  }

  @override
  Future<List<Song>> search(String providerId, String query) {
    return _report(delegate.search(providerId, query));
  }

  @override
  Future<List<NamedPlugin>> getProviders() {
    return _report(delegate.getProviders());
  }

  @override
  Future<void> removeSuggestion({
    @Path("suggesterId") String suggesterId,
    @Query("providerId") String providerId,
    @Query("songId") String songId,
  }) {
    return _report(delegate.removeSuggestion(
      suggesterId: suggesterId,
      providerId: providerId,
      songId: songId,
    ));
  }

  @override
  Future<List<Song>> getSuggestions(String suggesterId) {
    return _report(delegate.getSuggestions(suggesterId));
  }

  @override
  Future<List<NamedPlugin>> getSuggesters() {
    return _report(delegate.getSuggesters());
  }

  @override
  Future<List<SongEntry>> moveEntry(
    int index,
    String songId,
    String providerId,
  ) {
    return _report(delegate.moveEntry(index, songId, providerId));
  }

  @override
  Future<List<SongEntry>> getQueueHistory() {
    return _report(delegate.getQueueHistory());
  }

  @override
  Future<List<SongEntry>> getQueue() {
    return _report(delegate.getQueue());
  }

  @override
  Future<PlayerState> getPlayerState() {
    return _report(delegate.getPlayerState());
  }

  @override
  Future<Tokens> changePassword(PasswordChange passwordChange) {
    return _report(delegate.changePassword(passwordChange));
  }

  @override
  Future<Tokens> register(RegisterCredentials credentials) {
    return _report(delegate.register(credentials));
  }

  @override
  Future<Tokens> refresh(String bearer) {
    return _report(delegate.refresh(bearer));
  }
}
