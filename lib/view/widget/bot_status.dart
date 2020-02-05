import 'package:flutter/material.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/widget/loading_delegate.dart';

class BotStatus extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return LoadingDelegate(
      action: _loadVersion,
      itemBuilder: _buildInfo,
    );
  }

  Future<BotInfo> _loadVersion() async {
    final bot = await service<ConnectionManager>().getService();
    return await bot.getVersion();
  }

  Widget _buildInfo(BuildContext context, BotInfo info) {
    return Scrollbar(
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        child: Padding(
          padding: const EdgeInsets.all(8.0),
          child: Column(
            mainAxisSize: MainAxisSize.max,
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              _buildItem("API version:", Text(info.apiVersion)),
              Divider(),
              _ImplementationInfoView(info: info.implementation),
            ],
          ),
        ),
      ),
    );
  }
}

class _ImplementationInfoView extends StatelessWidget {
  final ImplementationInfo info;

  const _ImplementationInfoView({Key key, this.info}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          "Implementation:",
          textScaleFactor: 1.2,
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        Padding(
          padding: const EdgeInsets.only(left: 15),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              _buildItem("Name:", Text(info.name)),
              _buildItem("Version:", Text(info.version)),
              _buildItem("Info:", _buildLink(context, info.projectInfo)),
            ],
          ),
        ),
      ],
    );
  }
}

Widget _buildLink(BuildContext context, String value) {
  return InkWell(
      child: Text(
        value,
        style:
            TextStyle(decoration: TextDecoration.underline, color: Colors.blue),
      ),
      onTap: () {
        // TODO
        Scaffold.of(context).showSnackBar(SnackBar(
          content: Text("Sorry, URL opening is not supported yet."),
        ));
      });
}

Widget _buildItem(String name, Widget value) {
  return Row(
    mainAxisSize: MainAxisSize.max,
    children: <Widget>[
      Text(name, style: TextStyle(fontWeight: FontWeight.bold)),
      Container(width: 5),
      value,
    ],
  );
}
