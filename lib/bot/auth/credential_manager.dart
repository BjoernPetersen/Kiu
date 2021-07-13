import 'package:kiu/bot/bot.dart';

abstract class CredentialManager {
  Future<String?> getRefreshToken(Bot bot);
  Future<void> setRefreshToken(Bot bot, String token);
  Future<void> removeRefreshToken(Bot bot);
  Future<void> close();
}
