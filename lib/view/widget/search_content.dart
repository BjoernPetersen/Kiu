import 'package:async/async.dart';
import 'package:dio/dio.dart';
import 'package:flutter/cupertino.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/widget/loader.dart';
import 'package:kiu/view/widget/result_list.dart';

class SearchContent extends StatefulWidget {
  final String providerId;
  final TextEditingController query;

  const SearchContent(this.providerId, this.query) : super();

  @override
  State<StatefulWidget> createState() => _SearchContentState(providerId, query);
}

class _SearchContentState extends State<SearchContent> {
  final String providerId;
  final TextEditingController query;
  CancelableOperation operation;
  List<Song> results;

  _SearchContentState(this.providerId, this.query);

  @override
  void initState() {
    super.initState();
    if (query.text.isNotEmpty) {
      _onQueryChange();
    }
    query.addListener(_onQueryChange);
  }

  _onQueryChange() {
    operation?.cancel();
    setState(() {
      results = null;
    });
    final query = this.query.text;
    if (query.isEmpty) {
      operation = null;
    } else {
      operation = CancelableOperation.fromFuture(_search(query)).then((it) {
        if (it != null) {
          setState(() {
            results = it;
          });
        }
      });
    }
  }

  Future<List<Song>> _search(String query) async {
    await Future.delayed(Duration(seconds: 3));
    final connectionManager = service<ConnectionManager>();
    try {
      final bot = await connectionManager.getService();
      return await bot.search(providerId, query);
    } on DioError catch (e) {
      return null;
    }
  }

  @override
  Widget build(BuildContext context) {
    if (operation == null) {
      return Center(child: Text("Please enter a search query"));
    }
    if (results == null) {
      return Loader();
    }
    return ResultList(results: results);
  }
}
