import 'package:dio/dio.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/auth/credential_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state/bot_connection.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/input_dialog.dart';
import 'package:progress_dialog/progress_dialog.dart';

Future<void> askPassword(BuildContext context) async {
  final String? result = await showDialog(
    context: context,
    builder: (_) => InputDialog(
      hint: context.messages.login.password,
      obscureText: true,
    ),
  );
  if (result != null) {
    _tryChange(context, result);
  }
}

Future<void> _tryChange(BuildContext context, String password) async {
  final progress = ProgressDialog(context, isDismissible: false);
  final shown = progress.show();
  final accessManager = service<AccessManager>();
  try {
    final bot = service<BotConnection>().bot.lastValue;
    if (bot == null) {
      return;
    }
    final botService = await accessManager.createService();
    final tokens = await botService.changePassword(
      PasswordChange(newPassword: password),
    );
    service<CredentialManager>().setRefreshToken(bot, tokens.refreshToken!);
    accessManager.reset();
    // TODO update access token in manager
  } on DioError catch (e) {
    if (e.type == DioErrorType.response && e.response!.statusCode == 400) {
      Fluttertoast.showToast(msg: context.messages.login.errorPasswordShort);
      askPassword(context);
    } else {
      _showError(context, password);
    }
  } on RefreshTokenException {
    _showError(context, password);
  } finally {
    await shown;
    await progress.hide();
  }
}

_showError(BuildContext context, String password) {
  ScaffoldMessenger.of(context).showSnackBar(SnackBar(
    content: Text(context.messages.login.errorPasswordChange),
    action: SnackBarAction(
      label: context.messages.common.retry,
      onPressed: () => _tryChange(context, password),
    ),
  ));
}
