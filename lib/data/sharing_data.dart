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

SharingData extractSharingData(String url) {
  final uri = Uri.tryParse(url);
  if (uri != null) {
    switch (uri.host) {
      case "youtu.be":
        return _extractYoutubeShort(uri);
      case "youtube.com":
        return _extractYoutubeLong(uri);
      case "open.spotify.com":
        return _extractSpotify(uri);
    }
  }
  return null;
}
