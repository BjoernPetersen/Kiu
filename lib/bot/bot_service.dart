import 'package:dio/dio.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:retrofit/retrofit.dart';

part 'bot_service.g.dart';

@RestApi()
abstract class BotService {
  factory BotService(Dio dio, String baseUrl) =>
      _BotService(dio, baseUrl: baseUrl);

  @GET("/token")
  Future<Tokens> login(@Header("Authorization") String basic);

  @GET("/token")
  Future<Tokens> refresh(@Header("Authorization") String bearer);

  @POST("/user")
  Future<Tokens> register(@Body() RegisterCredentials credentials);

  @PUT("/user")
  Future<Tokens> changePassword(@Body() PasswordChange passwordChange);

  @GET("/player")
  Future<PlayerState> getPlayerState();

  @GET("/player/queue")
  Future<List<SongEntry>> getQueue();

  @GET("/player/queue/history")
  Future<List<SongEntry>> getQueueHistory();

  @PUT("/player/queue/order")
  Future<List<SongEntry>> moveEntry(
    @Query("index") int index,
    @Query("songId") String songId,
    @Query("providerId") String providerId,
  );

  @GET("/suggester")
  Future<List<NamedPlugin>> getSuggesters();

  @GET("/suggester/{id}")
  Future<List<Song>> getSuggestions(@Path("id") String suggesterId);

  @DELETE("/suggester/{suggesterId}")
  Future<void> removeSuggestion({
    @Path("suggesterId") required String suggesterId,
    @Query("providerId") required String providerId,
    @Query("songId") required String songId,
  });

  @GET("/provider")
  Future<List<NamedPlugin>> getProviders();

  @GET("/provider/{id}")
  Future<List<Song>> search(
    @Path("id") String providerId,
    @Query("query") String query,
  );

  @PUT("/player/queue")
  Future<List<SongEntry>> enqueue(
    @Query("songId") String songId,
    @Query("providerId") String providerId,
  );

  @DELETE("/player/queue")
  Future<List<SongEntry>> dequeue(
      @Query("songId") String songId, @Query("providerId") String providerId);

  @PUT("/player")
  Future<PlayerState> changePlayerState(@Body() PlayerStateChange change);

  @GET("/volume")
  Future<Volume> getVolume();

  @PUT("/volume")
  Future<Volume> setVolume(@Query("value") int volume);

  @GET("/version")
  Future<BotInfo> getVersion();
}

@Deprecated("For removal")
String? get baseUrl {
  final ip = Preference.bot_ip.getString();
  return ip == null ? null : "http://${Preference.bot_ip.getString()}:42945";
}
