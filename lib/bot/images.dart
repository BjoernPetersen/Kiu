import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state/bot_connection.dart';
import 'package:kiu/data/dependency_model.dart';

@Deprecated("Use Bot.albumArtUrl instead")
String albumArtLink(Song song) {
  final bot = service<BotConnection>().bot.lastValue;
  return bot?.albumArtUrl(song);
}
