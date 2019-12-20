import 'package:cached_network_image/cached_network_image.dart';
import 'package:flutter/material.dart';
import 'package:kiu/bot/images.dart';
import 'package:kiu/bot/model.dart';

class AlbumArt extends StatelessWidget {
  final Song song;

  const AlbumArt(this.song) : super();

  // TODO find better placeholder
  Widget _createPlaceholder() => Icon(Icons.music_note);

  @override
  Widget build(BuildContext context) {
    final albumUrl = albumArtLink(song);
    if (albumUrl == null) {
      return _createPlaceholder();
    } else {
      return CachedNetworkImage(
        imageUrl: albumUrl,
        placeholder: (ctx, url) => Stack(
          alignment: AlignmentDirectional.center,
          children: <Widget>[
            _createPlaceholder(),
            CircularProgressIndicator(),
          ],
        ),
        errorWidget: (ctx, url, err) => Icon(Icons.music_note),
      );
    }
  }
}
