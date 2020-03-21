import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state/live_state.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/page/overflow.dart';
import 'package:kiu/view/page/queue_page.dart';
import 'package:kiu/view/routing/unanimated_route.dart';
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
import 'file:///E:/JMusic/Kiu/lib/view/widget/search/search_content.dart';

class SearchPage extends StatefulWidget {
  @override
  _SearchPageState createState() => _SearchPageState();
}

class _SearchPageState extends State<SearchPage> {
  final query = TextEditingController();

  @override
  void dispose() {
    query.dispose();
    super.dispose();
  }

  void _onClearTap() {
    if (query.text.isEmpty) {
      final nav = Navigator.of(context);
      while (nav.canPop()) {
        nav.pop();
      }
      nav.pushReplacement(UnanimatedRoute((_) => QueuePage()));
    } else {
      query.clear();
    }
  }

  Widget _buildScaffold(
    BuildContext context, {
    Widget bottom,
    @required Widget title,
    @required Widget child,
  }) {
    return Scaffold(
      appBar: AppBar(
        automaticallyImplyLeading: false,
        title: title,
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
      bottomNavigationBar: NavigationBar(BottomCategory.search),
    );
  }

  Widget _buildLoading(BuildContext context) {
    return _buildScaffold(
      context,
      title: Text(context.messages.page.search),
      child: Loader(),
    );
  }

  Widget _buildEmpty(BuildContext context) {
    return _buildScaffold(
      context,
      title: Text(context.messages.page.search),
      child: EmptyState(
        text: context.messages.search.errorNoProviders,
      ),
    );
  }

  Widget _buildTabBar(BuildContext context, List<NamedPlugin> providers) {
    return TabBar(
        isScrollable: true,
        tabs: providers
            .map((provider) => Tab(
                  child: Text(
                    provider.name,
                    maxLines: 1,
                  ),
                ))
            .toList(growable: false));
  }

  Widget _buildWithValues(BuildContext context, List<NamedPlugin> providers) {
    return DefaultTabController(
      length: providers.length,
      initialIndex: _indexOf(providers, Preference.provider_id.getString()),
      child: _buildScaffold(
        context,
        bottom: _buildTabBar(context, providers),
        title: TextField(
          controller: query,
          maxLines: 1,
          autofocus: true,
          decoration: InputDecoration(
            hintText: context.messages.search.hint,
            suffix: IconButton(
              icon: Icon(Icons.clear),
              onPressed: _onClearTap,
            ),
          ),
        ),
        child: SaveTab(
          save: (index) =>
              Preference.provider_id.setString(providers[index].id),
          child: TabBarView(
            children: providers
                .map((provider) => SearchContent(provider.id, query))
                .toList(growable: false),
          ),
        ),
      ),
    );
  }

  Widget _build(BuildContext context, List<NamedPlugin> providers) {
    if (providers == null) {
      return _buildLoading(context);
    } else if (providers.isEmpty) {
      return _buildEmpty(context);
    } else {
      return _buildWithValues(context, providers);
    }
  }

  @override
  Widget build(BuildContext context) {
    final providerState = service<LiveState>().provider;
    return BotStateBuilder(
      state: providerState,
      builder: _build,
    );
  }
}

int _indexOf(List<NamedPlugin> list, String id) {
  final index = list.indexWhere((it) => it.id == id);
  return index == -1 ? 0 : index;
}
