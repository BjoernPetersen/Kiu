import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/login_service.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/widget/embedded_player.dart';
import 'package:kiu/view/widget/loader.dart';
import 'package:kiu/view/widget/loading_delegate.dart';
import 'package:kiu/view/widget/navigation_bar.dart';
import 'package:kiu/view/widget/save_tab.dart';
import 'package:kiu/view/widget/search_content.dart';

import 'overflow.dart';

class SearchPage extends StatefulWidget {
  @override
  _SearchPageState createState() => _SearchPageState();
}

class _SearchPageState extends State<SearchPage> {
  final query = TextEditingController();

  Future<List<NamedPlugin>> _loadProviders() async {
    final connectionManager = service<ConnectionManager>();
    try {
      final bot = await connectionManager.getService();
      return await bot.getProviders();
    } on DioError catch (e) {
      switch (e.type) {
        case DioErrorType.CONNECT_TIMEOUT:
        case DioErrorType.SEND_TIMEOUT:
        case DioErrorType.RECEIVE_TIMEOUT:
          connectionManager.reset();
          return [];
        default:
          return [];
      }
    } on IOException {
      connectionManager.reset();
      return [];
    } catch (e) {
      // TODO handle StateError
      print(e);
      return [];
    }
  }

  @override
  void dispose() {
    query.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => LoadingDelegate(
        action: _loadProviders,
        itemBuilder: (context, List<NamedPlugin> providers) =>
            DefaultTabController(
          length: providers.length,
          initialIndex: _indexOf(providers, Preference.provider_id.getString()),
          child: Scaffold(
            appBar: AppBar(
              leading: IconButton(
                icon: Icon(Icons.clear),
                onPressed: () => query.clear(),
              ),
              title: TextField(
                controller: query,
                maxLines: 1,
                autofocus: true,
                decoration: InputDecoration(hintText: 'Search'),
              ),
              actions: <Widget>[createOverflowItems(context)],
              bottom: TabBar(
                tabs: providers
                    .map((providers) => Tab(
                          child: Text(
                            providers.name,
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                            style: TextStyle(fontSize: 11),
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