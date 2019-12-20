import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/discovery_service.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/data/urls.dart';
import 'package:kiu/view/widget/input_dialog.dart';
import 'package:kiu/view/widget/loader.dart';
import 'package:kiu/view/page/overflow.dart';

import 'overflow.dart';

class BotPage extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _BotPageState();
  }
}

class _BotPageState extends State<BotPage> {
  bool _isLoading = false;
  List<String> _bots;

  Future<void> _refreshBots() async {
    if (_isLoading) return;
    setState(() {
      _isLoading = true;
    });
    // TODO handle failure
    final result = await service<DiscoveryService>().findBots();
    setState(() {
      _bots = result.toList(growable: false);
      _isLoading = false;
    });
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: _createAppBar(),
        body: _createBody(context),
      );

  Widget _createRefreshButton({double size = 24.0}) {
    return IconButton(
      iconSize: size,
      icon: Icon(Icons.refresh),
      onPressed: _refreshBots,
      tooltip: 'Refresh bots',
    );
  }

  AppBar _createAppBar() => AppBar(
        title: Text('Bot detection'),
        actions: <Widget>[
          _createRefreshButton(),
        ],

      );

  Future<void> _setIp(String ip) async {
    await Preference.bot_ip.setString(ip);
    service<ConnectionManager>().reset();
    Navigator.of(context).pop();
  }

  Future<void> _enterManually(context) async {
    final result = await showDialog(
      context: context,
      child: InputDialog(
        hint: 'Enter bot IP (e.g. 192.168.178.42)',
      ),
    );
    if (result != null) {
      final sanitizedIp = sanitizeHost(result);
      _setIp(sanitizedIp);
      Fluttertoast.showToast(
        msg: "Using IP: $sanitizedIp",
        toastLength: Toast.LENGTH_SHORT,
      );
    }
  }

  Widget _createManualButton(context) {
    return IconButton(
      icon: Icon(
        Icons.keyboard,
        size: 48,
      ),
      tooltip: 'Enter manually',
      onPressed: () => _enterManually(context),
    );
  }

  Widget _createBody(context) {
    if (_isLoading) {
      return Loader();
    }

    final bots = _bots;
    if (bots == null) {
      _refreshBots();
      return Loader();
    } else if (bots.isEmpty) {
      return Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Text(
              "No bots found.",
              textScaleFactor: 1.5,
            ),
            Row(
              mainAxisSize: MainAxisSize.min,
              children: <Widget>[
                _createRefreshButton(size: 48),
                _createManualButton(context)
              ],
            ),
          ],
        ),
      );
    } else {
      return ListView.builder(
        itemCount: bots.length,
        itemBuilder: (_, index) {
          final ip = bots[index];
          return ListTile(
            title: Text(ip),
            onTap: () => _setIp(ip),
          );
        },
      );
    }
  }
}
