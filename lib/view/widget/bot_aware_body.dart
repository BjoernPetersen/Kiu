import 'package:flutter/material.dart';
import 'package:kiu/data/preferences.dart';

class BotAwareBody extends StatelessWidget {
  final Widget child;

  const BotAwareBody({Key key, @required this.child}) : super(key: key);

  List<Widget> _buildChildren(BuildContext context) {
    final result = <Widget>[];
    if (Preference.bot_ip.getString() == null) {
      result.add(
        MaterialBanner(
          content: Text("No bot selected"),
          actions: <Widget>[
            FlatButton(
              child: Text("Fix it"),
              onPressed: () => Navigator.of(context).pushNamed("/selectBot"),
            ),
          ],
        ),
      );
    }
    result.add(Expanded(child: child));
    return result;
  }

  @override
  Widget build(BuildContext context) => Column(
        mainAxisSize: MainAxisSize.max,
        children: _buildChildren(context),
      );
}
