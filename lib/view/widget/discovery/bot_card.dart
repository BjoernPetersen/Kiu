import 'package:kiu/bot/bot.dart';
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
        title: Text(bot.version.botName),
        subtitle: Text(bot.ip),
        onTap: onTap,
      ),
    );
  }
}
