import 'package:flutter/material.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/widget/basic_awareness_body.dart';
import 'package:kiu/view/widget/bot_status.dart';
import 'package:kiu/view/widget/empty_state.dart';
import 'package:kiu/view/widget/volume_control.dart';

class StatePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final botIp = Preference.bot_ip.getString();
    final manager = service<ConnectionManager>();
    return Scaffold(
      appBar: AppBar(
        title: Text(_getTitleText(botIp)),
      ),
      body: BasicAwarenessBody(
        child: botIp == null ? _buildEmpty() : _buildBody(context),
      ),
    );
  }

  String _getTitleText(String botIp) {
    if (botIp == null) {
      return "Bot status";
    } else {
      return "Bot at $botIp";
    }
  }

  Widget _buildEmpty() => EmptyState(text: "Nothing to show");

  Widget _buildBody(BuildContext context) {
    return Row(
      mainAxisSize: MainAxisSize.max,
      children: <Widget>[
        Expanded(child: BotStatus()),
        VolumeControl(),
      ],
    );
  }
}

Widget createStateAction(BuildContext context) {
  return IconButton(
    icon: Icon(Icons.info),
    tooltip: "Show bot info",
    onPressed: () => Navigator.of(context).pushNamed("/state"),
  );
}
