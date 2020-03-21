import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state/live_state.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/basic_awareness_body.dart';
import 'package:kiu/view/widget/bot_state_builder.dart';
import 'package:kiu/view/widget/bot_status/status_action.dart';
import 'package:kiu/view/widget/embedded_player.dart';
import 'package:kiu/view/widget/empty_state.dart';
import 'package:kiu/view/widget/error_aware.dart';
import 'package:kiu/view/widget/loader.dart';
import 'package:kiu/view/widget/login_error_aware.dart';
import 'package:kiu/view/widget/navigation_bar.dart';
import 'package:kiu/view/widget/save_tab.dart';
import 'package:kiu/view/widget/suggestions/suggestions_content.dart';

import 'overflow.dart';

class SuggestionsPage extends StatelessWidget {
  Widget _buildTabBar(BuildContext context, List<NamedPlugin> suggesters) {
    return TabBar(
        isScrollable: true,
        tabs: suggesters
            .map((suggester) => Tab(
                  child: Text(
                    suggester.name,
                    maxLines: 1,
                  ),
                ))
            .toList(growable: false));
  }

  Widget _buildScaffold(
    BuildContext context, {
    Widget bottom,
    @required Widget child,
  }) {
    return Scaffold(
      appBar: AppBar(
        automaticallyImplyLeading: false,
        title: Text(context.messages.page.suggestion),
        actions: <Widget>[
          StatusAction(),
          createOverflowItems(context),
        ],
        bottom: bottom,
      ),
      body: ErrorAware(
        child: LoginErrorAware(
          child: EmbeddedPlayer(
            child: BasicAwarenessBody(
              child: child,
            ),
          ),
        ),
      ),
      bottomNavigationBar: NavigationBar(BottomCategory.suggestions),
    );
  }

  Widget _buildLoading(BuildContext context) {
    return _buildScaffold(context, child: Loader());
  }

  Widget _buildEmpty(BuildContext context) {
    return _buildScaffold(
      context,
      child: EmptyState(
        text: context.messages.suggestions.errorNoSuggesters,
      ),
    );
  }

  Widget _buildWithValues(BuildContext context, List<NamedPlugin> suggesters) {
    return DefaultTabController(
      length: suggesters.length,
      initialIndex: _indexOf(suggesters, Preference.suggester_id.getString()),
      child: _buildScaffold(
        context,
        bottom: _buildTabBar(context, suggesters),
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
    );
  }

  Widget _build(BuildContext context, List<NamedPlugin> suggesters) {
    if (suggesters == null) {
      return _buildLoading(context);
    } else if (suggesters.isEmpty) {
      return _buildEmpty(context);
    } else {
      return _buildWithValues(context, suggesters);
    }
  }

  @override
  Widget build(BuildContext context) {
    final suggesterState = service<LiveState>().suggester;
    return BotStateBuilder(
      state: suggesterState,
      builder: _build,
    );
  }
}

int _indexOf(List<NamedPlugin> list, String id) {
  final index = list.indexWhere((it) => it.id == id);
  return index == -1 ? 0 : index;
}
