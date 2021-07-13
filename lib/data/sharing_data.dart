import 'package:dio/dio.dart';

class SharingData {
  final String providerId;
  final String songId;

  const SharingData({required this.providerId, required this.songId});

  @override
  String toString() {
    return 'SharingData{providerId: $providerId, songId: $songId}';
  }
}

const String YOUTUBE_ID =
    'net.bjoernpetersen.musicbot.spi.plugin.predefined.youtube.YouTubeProvider';
const String SPOTIFY_ID =
    'net.bjoernpetersen.musicbot.spi.plugin.predefined.spotify.SpotifyProvider';

SharingData? _extractYoutubeLong(Uri uri) {
  String? songId = uri.queryParameters['v'];
  if (songId != null && songId.isNotEmpty) {
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

Future<SharingData?> _extractLinktoSpotify(Uri uri) async {
  final resolvedUri = await _followLinkTo(uri);
  if (resolvedUri == null) {
    print("Could not resolve link.tospotify.com");
    return null;
  }
  print("Extracted URI: $resolvedUri");
  return _extractSpotify(resolvedUri);
}

const _userAgent =
    "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:81.0) Gecko/20100101 Firefox/81.0";

Future<Uri?> _followLinkTo(Uri uri) async {
  Dio dio = Dio(
    BaseOptions(
      followRedirects: false,
      headers: {"User-Agent": _userAgent},
    ),
  );
  Uri? currentUri = uri;
  while (currentUri != null &&
      currentUri.host.toLowerCase() != "open.spotify.com") {
    Response response;
    try {
      response = await dio.getUri(currentUri);
    } on DioError catch (e) {
      if (e.type == DioErrorType.response) {
        response = e.response!;
        print("Got error with response (${response.statusCode})");
      } else {
        print("Didn't get 'error' response for 307");
        return null;
      }
    }
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

Future<SharingData?> extractSharingData(String url) async {
  final trimmed = url.trim().split(RegExp(r"\s", multiLine: true)).last;
  if (trimmed.isEmpty) {
    print("No URL found (empty)");
    return null;
  }
  print("Trying to parse: $trimmed");
  final uri = Uri.tryParse(trimmed);
  if (uri != null) {
    switch (uri.host.toLowerCase()) {
      case "youtu.be":
        return _extractYoutubeShort(uri);
      case "youtube.com":
        return _extractYoutubeLong(uri);
      case "open.spotify.com":
        return _extractSpotify(uri);
      case "link.tospotify.com":
        return await _extractLinktoSpotify(uri);
    }
  }
  print("Could not match host: $uri");
  return null;
}
