import 'package:flutter/material.dart';

class ConfirmationDialog extends StatelessWidget {
  final Widget title;
  final Widget content;
  final String confirmText;
  final String cancelText;

  const ConfirmationDialog({
    Key key,
    @required this.title,
    this.content,
    this.confirmText = "OK",
    this.cancelText = "Cancel",
  }) : super(key: key);

  @override
  Widget build(BuildContext context) => AlertDialog(
        title: title,
        content: content,
        actions: <Widget>[
          FlatButton(
            child: Text(cancelText),
            onPressed:() => Navigator.of(context).pop(false),
          ),

          FlatButton(
            child: Text(confirmText),
            onPressed: () => Navigator.of(context).pop(true),
          ),
        ],
      );
}
