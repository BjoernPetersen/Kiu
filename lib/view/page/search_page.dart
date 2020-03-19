import 'package:dio/dio.dart';
import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/page/overflow.dart';
import 'package:kiu/view/page/queue_page.dart';
import 'package:kiu/view/routing/unanimated_route.dart';
import 'package:kiu/view/widget/embedded_player.dart';
import 'package:kiu/view/widget/loader.dart';
import 'package:kiu/view/widget/loading_delegate.dart';
import 'package:kiu/view/widget/navigation_bar.dart';
import 'package:kiu/view/widget/save_tab.dart';
import 'package:kiu/view/widget/search_content.dart';

class SearchPage extends StatefulWidget {
  @override
  _SearchPageState createState() => _SearchPageState();
}

class _SearchPageState extends State<SearchPage> {
  final query = TextEditingController();
  Future<List<NamedPlugin>> _loader;

  Future<List<NamedPlugin>> _load(BuildContext context) {
    if (_loader == null) {
      _loader = _loadProviders(context);
    }
    return _loader;
  }

  Future<List<NamedPlugin>> _loadProviders(BuildContext context) async {
    final access = service<AccessManager>();
    try {
      final bot = await access.createService();
      return await bot.getProviders();
    } on DioError catch (e) {
      // TODO handle
      return [];
    } on MissingBotException {
      if (mounted) {
        Navigator.pushNamed(context, "/selectBot");
      }
      return [];
    } on RefreshTokenException {
      // TODO handle
      access.reset();
      return [];
    }
  }

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

  @override
  Widget build(BuildContext context) => LoadingDelegate(
        action: () => _load(context),
        itemBuilder: (context, List<NamedPlugin> providers) =>
            DefaultTabController(
          length: providers.length,
          initialIndex: _indexOf(providers, Preference.provider_id.getString()),
          child: Scaffold(
            appBar: AppBar(
              automaticallyImplyLeading: false,
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
              actions: <Widget>[createOverflowItems(context)],
              bottom: TabBar(
                isScrollable: true,
                tabs: providers
                    .map((providers) => Tab(
                          child: Text(
                            providers.name,
                            maxLines: 1,
                          ),
                        ))
                    .toList(growable: false),
              ),
            ),
            body: EmbeddedPlayer(
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
            bottomNavigationBar: NavigationBar(BottomCategory.search),
          ),
        ),
        loaderBuilder: (context) => Scaffold(
          appBar: AppBar(
            automaticallyImplyLeading: false,
            title: Text('Kiu'),
            actions: <Widget>[createOverflowItems(context)],
          ),
          body: EmbeddedPlayer(child: Loader()),
          bottomNavigationBar: NavigationBar(BottomCategory.search),
        ),
      );
}

int _indexOf(List<NamedPlugin> list, String id) {
  final index = list.indexWhere((it) => it.id == id);
  return index == -1 ? 0 : index;
}
