import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/resources/messages.i18n.dart';
import 'package:kiu/view/widget/basic_awareness_body.dart';
import 'package:kiu/view/widget/bot_status.dart';
import 'package:kiu/view/widget/empty_state.dart';
import 'package:kiu/view/widget/volume_control.dart';

class StatePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final botIp = Preference.bot_ip.getString();
    return Scaffold(
      appBar: AppBar(
        title: Text(_getTitleText(context.messages.page, botIp)),
      ),
      body: BasicAwarenessBody(
        child: botIp == null ? _buildEmpty(context) : _buildBody(context),
      ),
    );
  }

  String _getTitleText(PageMessages messages, String botIp) {
    if (botIp == null) {
      return messages.state;
    } else {
      return messages.stateOf(botIp);
    }
  }

  Widget _buildEmpty(BuildContext context) => EmptyState(
        text: context.messages.botState.empty,
      );

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
    tooltip: context.messages.botState.showInfo,
    onPressed: () => Navigator.of(context).pushNamed("/state"),
  );
}
