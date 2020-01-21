import 'package:flutter/material.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/widget/loading_delegate.dart';
import 'package:kiu/view/widget/result_list.dart';

class SuggestionsContent extends StatelessWidget {
  final NamedPlugin suggester;

  const SuggestionsContent(this.suggester) : super();

  Future<List<Song>> Function() _retrieve(ConnectionManager manager) {
    return () async {
      final bot = await manager.getService();
      return bot.getSuggestions(suggester.id);
    };
  }

  Future<void> _delete(ConnectionManager manager, Song song) async {
    final bot = await manager.getService();
    await bot.removeSuggestion(
        suggesterId: suggester.id,
        providerId: song.provider.id,
        songId: song.id);
    // TODO would be nice to remove from list here
  }

  @override
  Widget build(BuildContext context) {
    final manager = service<ConnectionManager>();
    if (manager.hasPermission(Permission.DISLIKE)) {
      return LoadingDelegate(
          action: _retrieve(manager),
          itemBuilder: (_, result) => ResultList(
                results: result,
                trailingBuilder: (_, song) {
                  return IconButton(
                    icon: Icon(Icons.delete),
                    onPressed: () => _delete(manager, song),
                  );
                },
              ));
    } else {
      return LoadingDelegate(
          action: _retrieve(manager),
          itemBuilder: (_, result) => ResultList(results: result));
    }
  }
}
