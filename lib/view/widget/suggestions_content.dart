import 'package:flutter/material.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/widget/loading_delegate.dart';
import 'package:kiu/view/widget/result_list.dart';

class SuggestionsContent extends StatelessWidget {
  final NamedPlugin suggester;

  const SuggestionsContent(this.suggester) : super();

  Future<List<Song>> _retrieve() async {
    final bot = await service<ConnectionManager>().getService();
    return bot.getSuggestions(suggester.id);
  }

  @override
  Widget build(BuildContext context) => LoadingDelegate(
      action: _retrieve,
      itemBuilder: (_, result) => ResultList(results: result));
}
