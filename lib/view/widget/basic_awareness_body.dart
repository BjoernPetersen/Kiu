import 'package:app_settings/app_settings.dart';
import 'package:connectivity/connectivity.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/common.dart';

class BasicAwarenessBody extends StatefulWidget {
  final Widget child;
  final Set<Requirement>? require;

  const BasicAwarenessBody({
    required this.child,
    this.require,
  }) : super();

  @override
  _BasicAwarenessBodyState createState() => _BasicAwarenessBodyState();

  bool requires(Requirement requirement) {
    return require?.contains(requirement) ?? true;
  }
}

class _BasicAwarenessBodyState extends State<BasicAwarenessBody> {
  bool _isConnected = true;

  Future<void> _checkWifiEnabled() async {
    final state = await Connectivity().checkConnectivity();
    setState(() {
      _isConnected = (state == ConnectivityResult.wifi);
    });
  }

  List<Widget> _buildChildren(BuildContext context) {
    final result = <Widget>[];
    final msg = context.messages.requirement;
    if (widget.requires(Requirement.bot_ip) &&
        Preference.bot_ip.getString() == null) {
      result.add(
        MaterialBanner(
          leading: Icon(
            Icons.warning,
            color: Colors.red,
          ),
          content: Text(msg.bot.description),
          actions: <Widget>[
            FlatButton(
              child: Text(msg.bot.button),
              onPressed: () => Navigator.of(context).pushNamed("/selectBot"),
            ),
          ],
        ),
      );
    } else if (widget.requires(Requirement.wifi) && !_isConnected) {
      result.add(MaterialBanner(
        leading: Icon(
          Icons.signal_wifi_off,
          color: Colors.red,
        ),
        content: Text(msg.wifi.description),
        actions: <Widget>[
          FlatButton(
            child: Text(msg.wifi.button),
            onPressed: AppSettings.openWIFISettings,
          )
        ],
      ));
    }
    result.add(Expanded(child: widget.child));
    return result;
  }

  @override
  Widget build(BuildContext context) {
    _checkWifiEnabled();
    return Column(
      mainAxisSize: MainAxisSize.max,
      children: _buildChildren(context),
    );
  }
}

enum Requirement {
  bot_ip,
  wifi,
}
