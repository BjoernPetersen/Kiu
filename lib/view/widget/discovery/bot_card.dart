import 'package:kiu/bot/bot.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/view/common.dart';

class BotCard extends StatelessWidget {
  final Bot bot;
  final Function() onTap;

  const BotCard({
    Key key,
    @required this.bot,
    @required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Card(
      child: ListTile(
        title: _buildTitle(),
        subtitle: Text(bot.ip),
        onTap: onTap,
      ),
    );
  }

  Widget _buildTitle() {
    return FutureBuilder(
      future: bot.version,
      builder: (ctx, AsyncSnapshot<BotInfo> snapshot) {
        if (snapshot.hasData) {
          return Text(snapshot.data.botName);
        } else if (snapshot.hasError) {
          return Text(ctx.messages.bot.errorInfo);
        } else {
          return Text(ctx.messages.bot.loadingName);
        }
      },
    );
  }
}
