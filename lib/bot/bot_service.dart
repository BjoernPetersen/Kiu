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
}

String get baseUrl {
  final ip = Preference.bot_ip.getString();
  return ip == null ? null : "http://${Preference.bot_ip.getString()}";
}
