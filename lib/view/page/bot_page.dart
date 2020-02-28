import 'package:flutter/material.dart';
import 'package:kiu/view/widget/basic_awareness_body.dart';
import 'package:kiu/view/widget/discovery/discovery_content.dart';
import 'package:kiu/view/widget/discovery/discovery_controller.dart';
import 'package:kiu/view/widget/discovery/discovery_refresh_button.dart';
import 'package:kiu/view/widget/discovery/manual_discovery_button.dart';
import 'package:provider/provider.dart';

class BotPage extends StatefulWidget {
  @override
  _BotPageState createState() => _BotPageState();
}

class _BotPageState extends State<BotPage> {
  DiscoveryController controller;

  @override
  void initState() {
    super.initState();
    controller = DiscoveryController();
    controller.onUpdate = () => setState(() {});
    controller.refresh();
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) => Provider(
        create: (_) => controller,
        child: Scaffold(
          appBar: _createAppBar(),
          body: BasicAwarenessBody(
            child: DiscoveryContent(),
            require: {Requirement.wifi},
          ),
        ),
      );

  AppBar _createAppBar() => AppBar(
        title: Text('Bot detection'),
        actions: <Widget>[
          ManualDiscoveryButton(),
          DiscoveryRefreshButton(),
        ],
      );
}
