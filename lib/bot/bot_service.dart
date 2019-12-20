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
  Future<String> login(@Header("Authorization") String basic);

  @POST("/user")
  Future<String> register(@Body() RegisterCredentials credentials);

  @GET("/player")
  Future<PlayerState> getPlayerState();

  @GET("/player/queue")
  Future<List<SongEntry>> getQueue();

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

  @PUT("/player/queue")
  Future<List<SongEntry>> enqueue(
      @Query("songId") String songId, @Query("providerId") String providerId);

  @PUT("/player")
  Future<PlayerState> changePlayerState(@Body() PlayerStateChange change);
}

String get baseUrl {
  final ip = Preference.bot_ip.getString();
  return ip == null ? null : "http://${Preference.bot_ip.getString()}:42945";
}
