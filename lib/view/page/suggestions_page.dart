import 'package:flutter/material.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/page/overflow.dart';
import 'package:kiu/view/widget/embedded_player.dart';
import 'package:kiu/view/widget/loader.dart';
import 'package:kiu/view/widget/loading_delegate.dart';
import 'package:kiu/view/widget/navigation_bar.dart';
import 'package:kiu/view/widget/save_tab.dart';
import 'package:kiu/view/widget/suggestions_content.dart';

class SuggestionsPage extends StatelessWidget {
  Future<List<NamedPlugin>> _loadSuggesters(BuildContext context) async {
    try {
      final bot = await service<ConnectionManager>().getService();
      return await bot.getSuggesters();
    } on StateError {
      Navigator.of(context).pushNamed("/selectBot");
      return [];
    } catch (e) {
      // TODO handle this
      print(e);
      return [];
    }
  }

  Widget build(BuildContext context) => LoadingDelegate(
        action: () => _loadSuggesters(context),
        itemBuilder: (context, List<NamedPlugin> suggesters) =>
            DefaultTabController(
          length: suggesters.length,
          initialIndex:
              _indexOf(suggesters, Preference.suggester_id.getString()),
          child: Scaffold(
            appBar: AppBar(
              automaticallyImplyLeading: false,
              title: Text('Kiu'),
              actions: <Widget>[createOverflowItems(context)],
              bottom: TabBar(
                isScrollable: true,
                tabs: suggesters
                    .map((suggester) => Tab(
                          child: Text(
                            suggester.name,
                            maxLines: 1,
                          ),
                        ))
                    .toList(growable: false),
              ),
            ),
            body: EmbeddedPlayer(
              child: SaveTab(
                save: (index) =>
                    Preference.suggester_id.setString(suggesters[index].id),
                child: TabBarView(
                  children: suggesters
                      .map((suggester) => SuggestionsContent(suggester))
                      .toList(growable: false),
                ),
              ),
            ),
            bottomNavigationBar: NavigationBar(BottomCategory.suggestions),
          ),
        ),
        loaderBuilder: (context) => Scaffold(
          appBar: AppBar(
            automaticallyImplyLeading: false,
            title: Text('Kiu'),
            actions: <Widget>[createOverflowItems(context)],
          ),
          body: EmbeddedPlayer(child: Loader()),
          bottomNavigationBar: NavigationBar(BottomCategory.suggestions),
        ),
      );
}

int _indexOf(List<NamedPlugin> list, String id) {
  final index = list.indexWhere((it) => it.id == id);
  return index == -1 ? 0 : index;
}
