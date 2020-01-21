import 'package:flutter/material.dart';

class BotCard extends StatelessWidget {
  final String ip;
  final Function() onTap;

  const BotCard({
    Key key,
    @required this.ip,
    @required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) => Card(
        child: ListTile(
          title: Text("Bot at ip $ip"),
          subtitle: Text("Tap to use this bot"),
          onTap: onTap,
        ),
      );
}
