import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/loader.dart';
import 'package:kiu/view/widget/retry_content.dart';
import 'package:kiu/view/widget/suggestions/suggestion_list.dart';

class SuggestionsContent extends StatefulWidget {
  final NamedPlugin suggester;

  const SuggestionsContent(this.suggester) : super();

  @override
  _SuggestionsContentState createState() => _SuggestionsContentState();
}

class _SuggestionsContentState extends State<SuggestionsContent> {
  Future<List<Song>> _suggestions;

  @override
  initState() {
    super.initState();
    _suggestions = _loadSuggestions();
  }

  Future<List<Song>> _loadSuggestions() async {
    final bot = await service<AccessManager>().createService();
    return await bot.getSuggestions(widget.suggester.id);
  }

  @override
  Widget build(BuildContext context) {
    return Provider(
      create: (_) => widget.suggester,
      lazy: false,
      child: FutureBuilder(
        future: _suggestions,
        builder: (context, snapshot) {
          if (snapshot.hasError) {
            return RetryContent(
              text: context.messages.suggestions.errorNoResult,
              refresh: () => setState(() {
                _suggestions = _loadSuggestions();
              }),
            );
          } else if (snapshot.hasData) {
            return SuggestionList(snapshot.data);
          } else {
            return Loader();
          }
        },
      ),
    );
  }
}
