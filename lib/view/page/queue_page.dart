import 'package:kiu/view/common.dart';
import 'package:kiu/view/page/overflow.dart';
import 'package:kiu/view/widget/basic_awareness_body.dart';
import 'package:kiu/view/widget/bot_state/state_action.dart';
import 'package:kiu/view/widget/embedded_player.dart';
import 'package:kiu/view/widget/error_aware.dart';
import 'package:kiu/view/widget/login_error_aware.dart';
import 'package:kiu/view/widget/navigation_bar.dart';
import 'package:kiu/view/widget/queue_list.dart';

class QueuePage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
        appBar: AppBar(
          title: Text(context.messages.page.queue),
          actions: <Widget>[
            StateAction(),
            createOverflowItems(context),
          ],
        ),
        body: ErrorAware(
          child: LoginErrorAware(
            child: EmbeddedPlayer(
              child: BasicAwarenessBody(
                child: QueueList(),
              ),
            ),
          ),
        ),
        bottomNavigationBar: NavigationBar(BottomCategory.queue));
  }
}
