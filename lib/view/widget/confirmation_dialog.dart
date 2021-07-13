import 'package:kiu/view/common.dart';

class ConfirmationDialog extends StatelessWidget {
  final Widget title;
  final Widget? content;
  final String? confirmText;
  final String? cancelText;

  const ConfirmationDialog({
    required this.title,
    this.content,
    this.confirmText,
    this.cancelText,
  }) : super();

  @override
  Widget build(BuildContext context) => AlertDialog(
        title: title,
        content: content,
        actions: <Widget>[
          FlatButton(
            child: Text(cancelText ?? context.messages.dialog.cancel),
            onPressed: () => Navigator.of(context).pop(false),
          ),
          FlatButton(
            child: Text(confirmText ?? context.messages.dialog.ok),
            onPressed: () => Navigator.of(context).pop(true),
          ),
        ],
      );
}
