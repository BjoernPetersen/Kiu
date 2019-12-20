import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/model.dart';

String albumArtLink(Song song) {
  final _baseUrl = baseUrl;
  final path = song.albumArtPath;
  if (_baseUrl == null || path == null) return null;
  return "$_baseUrl$path";
}
