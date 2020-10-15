import 'package:dio/dio.dart';

class SharingData {
  final String providerId;
  final String songId;

  const SharingData({this.providerId, this.songId});

  @override
  String toString() {
    return 'SharingData{providerId: $providerId, songId: $songId}';
  }
}

const String YOUTUBE_ID =
    'net.bjoernpetersen.musicbot.spi.plugin.predefined.youtube.YouTubeProvider';
const String SPOTIFY_ID =
    'net.bjoernpetersen.musicbot.spi.plugin.predefined.spotify.SpotifyProvider';

SharingData _extractYoutubeLong(Uri uri) {
  String songId = uri.queryParameters['v'];
  if (songId?.isNotEmpty == true) {
    return SharingData(providerId: YOUTUBE_ID, songId: songId);
  } else {
    return null;
  }
}

SharingData _extractYoutubeShort(Uri uri) {
  return SharingData(providerId: YOUTUBE_ID, songId: uri.pathSegments[0]);
}

SharingData _extractSpotify(Uri uri) {
  return SharingData(providerId: SPOTIFY_ID, songId: uri.pathSegments[1]);
}

Future<SharingData> _extractLinktoSpotify(Uri uri) async {
  final resolvedUri = await _followLinkTo(uri);
  if (resolvedUri== null) {
    print("Could not resolve linkto.spotify.com");
    return null;
  }
  return _extractSpotify(resolvedUri);
}

Future<Uri> _followLinkTo(Uri uri) async {
  Dio dio = Dio(BaseOptions(followRedirects: false));
  Uri currentUri = uri;
  for (int i = 0; currentUri != null && i<2; ++i) {
    final response = await dio.getUri(currentUri);
    if (response.statusCode != 307) {
      print("Didn't get 307 for URI: $currentUri");
      return null;
    }
    final location = response.headers.value('Location');
    if (location == null) {
      print("Got no location header for redirect");
      return null;
    }
    currentUri = Uri.tryParse(location);
  }
  return currentUri;
}

Future<SharingData> extractSharingData(String url) async {
  final trimmed = url.trim().split(RegExp(r"\s", multiLine: true)).last;
  if (trimmed.isEmpty) return null;
  final uri = Uri.tryParse(trimmed);
  if (uri != null) {
    switch (uri.host.toLowerCase()) {
      case "youtu.be":
        return _extractYoutubeShort(uri);
      case "youtube.com":
        return _extractYoutubeLong(uri);
      case "open.spotify.com":
        return _extractSpotify(uri);
      case "linkto.spotify.com":
        return await _extractLinktoSpotify(uri);
    }
  }
  return null;
}
