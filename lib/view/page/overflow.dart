import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/auth/credential_manager.dart';
import 'package:kiu/bot/state/bot_connection.dart';
import 'package:kiu/bot/state/login_error_state.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/page/password_dialog.dart';
import 'package:kiu/view/resources/messages.i18n.dart';

Widget createOverflowItems(
  BuildContext context, {
  List<Choice> hidden = const [],
}) =>
    Builder(
      builder: (context) => PopupMenuButton<Choice>(
        itemBuilder: (_) => Choice.values
            .where((it) => !hidden.contains(it))
            .map(
              (it) => PopupMenuItem<Choice>(
                value: it,
                child: Text(it.text(context.messages.overflow)),
              ),
            )
            .toList(growable: false),
        onSelected: (choice) {
          final navigator = Navigator.of(context);
          switch (choice) {
            case Choice.refresh_token:
              _refreshToken(context);
              break;
            case Choice.choose_bot:
              while (navigator.canPop()) {
                navigator.pop();
              }
              navigator.pushNamed("/selectBot");
              break;
            case Choice.set_password:
              askPassword(context);
              break;
            case Choice.logout:
              Preference.username.remove();
              service<AccessManager>().reset();
              final bot = service<BotConnection>().bot.lastValue;
              if (bot != null) {
                service<CredentialManager>().removeRefreshToken(bot);
              }
              navigator.pushReplacementNamed("/login");
              break;
          }
        },
      ),
    );

Future<void> _refreshToken(BuildContext context) async {
  final am = service<AccessManager>()..reset();
  try {
    await am.createService();
  } on RefreshTokenException catch (e) {
    service<LoginErrorState>().update(e);
  }
}

enum Choice {
  refresh_token,
  choose_bot,
  set_password,
  logout,
}

extension on Choice {
  // ignore: missing_return
  String text(OverflowMessages messages) {
    switch (this) {
      case Choice.refresh_token:
        return messages.refreshToken;
      case Choice.choose_bot:
        return messages.chooseBot;
      case Choice.set_password:
        return messages.setPassword;
      case Choice.logout:
        return messages.logout;
    }
  }
}
