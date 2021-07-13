import 'package:kiu/bot/bot.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/loader.dart';
import 'package:kiu/view/widget/retry_content.dart';
import 'package:url_launcher/url_launcher.dart';

class BotStatus extends StatefulWidget {
  @override
  _BotStatusState createState() => _BotStatusState();
}

class _BotStatusState extends State<BotStatus> {
  @override
  Widget build(BuildContext context) {
    return FutureBuilder<BotInfo>(
      future: Provider.of<Bot>(context).version,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.done) {
          if (snapshot.hasError) {
            return RetryContent(
              text: context.messages.state.errorNoInfo,
              refresh: () => setState(() {}),
            );
          } else if (snapshot.hasData) {
            return _buildInfo(context, snapshot.data!);
          }
        }
        return Loader();
      },
    );
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

  const _ImplementationInfoView({required this.info}) : super();

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
          ScaffoldMessenger.of(context).showSnackBar(SnackBar(
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
