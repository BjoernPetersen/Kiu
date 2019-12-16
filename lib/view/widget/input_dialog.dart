import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class InputDialog extends StatefulWidget {
  final String hint;

  InputDialog({this.hint});

  @override
  State<StatefulWidget> createState() => new _InputDialogState(hint: hint);
}

class _InputDialogState extends State<InputDialog> {
  final TextEditingController _controller = TextEditingController();
  final String hint;
  bool _isBlankError = false;

  _InputDialogState({this.hint});

  @override
  void initState() {
    super.initState();
    _controller.addListener(() => setState(() {
          _isBlankError = false;
        }));
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Dialog(
        child: Card(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              TextField(
                autocorrect: false,
                autofocus: true,
                controller: _controller,
                decoration: InputDecoration(
                  hintText: hint,
                  errorText: _isBlankError ? "Can't be blank" : null,
                ),
              ),
              ButtonBar(
                children: <Widget>[
                  FlatButton(
                    child: Text("Cancel"),
                    onPressed: Navigator.of(context).pop,
                  ),
                  FlatButton(
                    child: Text('OK'),
                    onPressed: () {
                      final text = _controller.value.text.trim();
                      if (text.isEmpty) {
                        setState(() {
                          _isBlankError = true;
                        });
                      } else {
                        Navigator.of(context).pop(_controller.value.text);
                      }
                    },
                  ),
                ],
              )
            ],
          ),
        ),
      );
}
