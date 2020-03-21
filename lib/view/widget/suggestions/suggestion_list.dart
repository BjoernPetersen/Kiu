import 'package:dio/dio.dart';
import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/bot/state/error_state.dart';
import 'package:kiu/bot/state/login_error_state.dart';
import 'package:kiu/data/action_error.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/result_list.dart';

class SuggestionList extends StatefulWidget {
  final List<Song> suggestions;

  const SuggestionList(this.suggestions) : super();

  @override
  State<StatefulWidget> createState() => _SuggestionListState(suggestions);
}

class _SuggestionListState extends State<SuggestionList> {
  List<Song> suggestions;

  _SuggestionListState(this.suggestions);

  Future<void> _delete(
    AccessManager manager,
    NamedPlugin suggester,
    Song song,
  ) async {
    try {
      final bot = await manager.createService();
      await bot.removeSuggestion(
          suggesterId: suggester.id,
          providerId: song.provider.id,
          songId: song.id);
      setState(() {
        suggestions.remove(song);
      });
    } on RefreshTokenException catch (e) {
      service<LoginErrorState>().update(e);
    } on DioError {
      service<ErrorState>().update(ActionError(
        errorText: (messages) => messages.suggestions.remove.error,
        action: (ctx, s) => SnackBarAction(
          label: ctx.messages.common.retry,
          onPressed: () => _delete(manager, suggester, song),
        ),
      ));
    }
  }

  @override
  Widget build(BuildContext context) {
    final manager = service<AccessManager>();
    if (manager.hasPermission(Permission.DISLIKE)) {
      final suggester = Provider.of<NamedPlugin>(context);
      return ResultList(
        results: suggestions,
        trailingBuilder: (_, song) {
          return IconButton(
            tooltip: context.messages.suggestions.remove.tooltip,
            icon: Icon(Icons.thumb_down),
            onPressed: () => _delete(manager, suggester, song),
          );
        },
      );
    } else {
      return ResultList(results: suggestions);
    }
  }
}
