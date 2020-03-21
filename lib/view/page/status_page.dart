import 'package:kiu/bot/bot.dart';
import 'package:kiu/bot/state/bot_connection.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';
import 'package:kiu/view/resources/messages.i18n.dart';
import 'package:kiu/view/widget/basic_awareness_body.dart';
import 'package:kiu/view/widget/bot_status/bot_status.dart';
import 'package:kiu/view/widget/empty_state.dart';
import 'package:kiu/view/widget/volume_control.dart';

class StatusPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    final bot = service<BotConnection>().bot.lastValue;
    return Provider(
      create: (_) => bot,
      child: Scaffold(
        appBar: AppBar(
          title: _getTitleText(context.messages.page, bot),
        ),
        body: BasicAwarenessBody(
          child: bot == null ? _buildEmpty(context) : _buildBody(context),
        ),
      ),
    );
  }

  Widget _getTitleText(PageMessages messages, Bot bot) {
    if (bot == null) {
      return Text(messages.state);
    } else {
      return FutureBuilder(
        future: bot.version,
        builder: (context, snapshot) {
          if (snapshot.hasData) {
            return Text(snapshot.data.botName);
          } else {
            return Text(messages.stateOfIp(bot.ip));
          }
        },
      );
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
