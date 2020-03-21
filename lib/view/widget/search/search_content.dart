import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state/login_error_state.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/empty_state.dart';
import 'package:kiu/view/widget/loader.dart';
import 'package:kiu/view/widget/result_list.dart';
import 'package:kiu/view/widget/retry_content.dart';

class SearchContent extends StatefulWidget {
  final String providerId;
  final TextEditingController query;

  const SearchContent(this.providerId, this.query) : super();

  @override
  State<StatefulWidget> createState() => _SearchContentState();
}

class _SearchContentState extends State<SearchContent> {
  String lastQuery;
  Future<List<Song>> search;

  _SearchContentState();

  @override
  void initState() {
    super.initState();
    _updateQuery();
    search = _search(lastQuery);
    widget.query.addListener(_onQueryChange);
  }

  @override
  void dispose() {
    widget.query.removeListener(_onQueryChange);
    super.dispose();
  }

  bool _updateQuery() {
    final query = widget.query.text.trim();
    if (query == lastQuery) return false;
    lastQuery = query;
    return true;
  }

  _onQueryChange() {
    if (_updateQuery()) {
      setState(() {
        search = _search(lastQuery);
      });
    }
  }

  Future<List<Song>> _search(String query) async {
    if (query.isEmpty) {
      return null;
    }

    final accessManager = service<AccessManager>();
    final bot = await accessManager.createService();
    return await bot.search(widget.providerId, query);
  }

  @override
  Widget build(BuildContext context) {
    return FutureBuilder(
      future: search,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          if (lastQuery.isEmpty) {
            return Center(child: Text(context.messages.search.noQuery));
          } else if (snapshot.hasError) {
            final error = snapshot.error;
            if (error is RefreshTokenException) {
              service<LoginErrorState>().update(error);
            }
            return RetryContent(
              text: context.messages.search.errorNoResult,
              refresh: () {
                setState(() {
                  search = _search(lastQuery);
                });
              },
            );
          } else if (snapshot.hasData) {
            final data = snapshot.data;
            if (data.isEmpty) {
              return EmptyState(text: context.messages.search.empty);
            } else {
              return ResultList(results: data);
            }
          }
        }
        return Loader();
      },
    );
  }
}
