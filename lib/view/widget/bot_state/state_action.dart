import 'package:kiu/view/common.dart';
import 'package:kiu/view/widget/bot_state/bot_state_icon.dart';

class StateAction extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return IconButton(
      icon: BotStateIcon(),
      tooltip: context.messages.botState.showInfo,
      onPressed: () => Navigator.of(context).pushNamed("/state"),
    );
  }
}
