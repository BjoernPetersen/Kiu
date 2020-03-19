import 'package:flutter/material.dart';
import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/widget/loading_delegate.dart';
import 'package:kiu/view/widget/suggestion_list.dart';

class SuggestionsContent extends StatelessWidget {
  final NamedPlugin suggester;

  const SuggestionsContent(this.suggester) : super();

  Future<List<Song>> _retrieve() async {
    final bot = await service<AccessManager>().createService();
    return bot.getSuggestions(suggester.id);
  }

  @override
  Widget build(BuildContext context) {
    return LoadingDelegate(
        action: _retrieve,
        itemBuilder: (_, result) => SuggestionList(
              suggester: suggester,
              suggestions: result,
            ));
  }
}
