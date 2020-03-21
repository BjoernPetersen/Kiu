import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/bot_status/bot_status_icon.dart';

class StatusAction extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: BotStatusIcon(),
      tooltip: context.messages.botState.showInfo,
      onPressed: () => Navigator.of(context).pushNamed("/state"),
    );
  }
}
