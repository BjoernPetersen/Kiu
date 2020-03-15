import 'package:dio/dio.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/login_service.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/input_dialog.dart';
import 'package:progress_dialog/progress_dialog.dart';

Future<void> askPassword(BuildContext context) async {
  final String result = await showDialog(
    context: context,
    builder: (_) => InputDialog(hint: context.messages.login.password),
  );
  _tryChange(context, result);
}

Future<void> _tryChange(BuildContext context, String password) async {
  final progress = ProgressDialog(context, isDismissible: false);
  final shown = progress.show();
  final connectionManager = service<ConnectionManager>();
  try {
    final bot = await connectionManager.getService();
    final tokens = await bot.changePassword(
      PasswordChange(newPassword: password),
    );
    Preference.refresh_token.setString(tokens.refreshToken);
    connectionManager.reset();
    Preference.token.setString(tokens.accessToken);
  } on DioError catch (e) {
    if (e.type == DioErrorType.RESPONSE && e.response.statusCode == 400) {
      Fluttertoast.showToast(msg: context.messages.login.errorPasswordShort);
      askPassword(context);
    } else {
      _showError(context, password);
    }
  } on LoginException {
    _showError(context, password);
  } finally {
    await shown;
    await progress.hide();
  }
}

_showError(BuildContext context, String password) {
  Scaffold.of(context).showSnackBar(SnackBar(
    content: Text(context.messages.login.errorPasswordChange),
    action: SnackBarAction(
      label: context.messages.common.retry,
      onPressed: () => _tryChange(context, password),
    ),
  ));
}
