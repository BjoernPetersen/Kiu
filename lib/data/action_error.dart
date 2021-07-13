import 'package:kiu/view/common.dart';
import 'package:kiu/view/resources/messages.i18n.dart';

class ActionError {
  final String Function(Messages) errorText;
  final SnackBarAction Function(BuildContext, Messages)? action;

  ActionError({required this.errorText, this.action});

  SnackBar toSnackBar(BuildContext context) {
    final messages = context.messages;
    final action = this.action;
    return SnackBar(
      content: Text(errorText(messages)),
      action: action == null ? null : action(context, messages),
    );
  }
}
