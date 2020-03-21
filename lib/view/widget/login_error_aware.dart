import 'dart:async';

import 'package:fluttertoast/fluttertoast.dart';
import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/auth/credential_manager.dart';
import 'package:kiu/bot/state/bot_connection.dart';
import 'package:kiu/bot/state/login_error_state.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';

class LoginErrorAware extends StatefulWidget {
  final Widget child;
  final bool ignoreMissingToken;

  const LoginErrorAware({Key key, this.child, this.ignoreMissingToken = false})
      : super(key: key);

  @override
  _LoginErrorAwareState createState() => _LoginErrorAwareState();
}

class _LoginErrorAwareState extends State<LoginErrorAware> {
  StreamSubscription<RefreshTokenException> _sub;

  @override
  void initState() {
    super.initState();

    final errorState = service<LoginErrorState>();
    _sub = errorState.stream.listen((event) {
      if (event != null && this.mounted) {
        _moveToLogin(event);
      }
    });
  }

  popEverything(NavigatorState nav) {
    while (nav.canPop()) {
      nav.pop();
    }
  }

  Future<void> _moveToLogin(RefreshTokenException error) async {
    service<AccessManager>().reset();
    if (error is MissingBotException) {
      Fluttertoast.showToast(msg: context.messages.refresh.errorNoBot);
      final nav = Navigator.of(context);
      popEverything(nav);
      await nav.pushReplacementNamed("/selectBot");
    } else {
      if (error is MissingRefreshTokenException && widget.ignoreMissingToken) {
        return;
      }
      final bot = service<BotConnection>().bot.lastValue;
      if (bot != null) {
        service<CredentialManager>().removeRefreshToken(bot);
      }

      Fluttertoast.showToast(msg: context.messages.refresh.errorLoginAgain);
      final nav = Navigator.of(context);
      popEverything(nav);
      await nav.pushReplacementNamed("/login");
    }
  }

  @override
  Widget build(BuildContext context) {
    return widget.child;
  }

  @override
  void dispose() {
    _sub.cancel();
    super.dispose();
  }
}
