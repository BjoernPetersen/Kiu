import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state/bot_connection.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/loading_delegate.dart';
import 'package:url_launcher/url_launcher.dart';

class BotStatus extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return LoadingDelegate(
      action: _loadVersion,
      itemBuilder: _buildInfo,
    );
  }

  Future<BotInfo> _loadVersion() async {
    final bot = service<BotConnection>().bot.lastValue;
    // TODO the return type is assumed to be non-null
    return await bot?.version;
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
              _buildItem(
                context.messages.state.apiVersion,
                Text(info.apiVersion),
              ),
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
    final msg = context.messages.state.implementation;
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: <Widget>[
        Text(
          msg.root,
          textScaleFactor: 1.2,
          style: TextStyle(fontWeight: FontWeight.bold),
        ),
        Padding(
          padding: const EdgeInsets.only(left: 15),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: <Widget>[
              _buildItem(msg.name, Text(info.name)),
              _buildItem(msg.version, Text(info.version)),
              _buildItem(msg.info, _buildLink(context, info.projectInfo)),
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
      onTap: () async {
        if (await canLaunch(value)) {
          await launch(value);
        } else {
          Scaffold.of(context).showSnackBar(SnackBar(
            content: Text(context.messages.state.urlError),
          ));
        }
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
