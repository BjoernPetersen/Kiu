import 'package:kiu/view/common.dart';

class BotCard extends StatelessWidget {
  final String ip;
  final Function() onTap;

  const BotCard({
    Key key,
    @required this.ip,
    @required this.onTap,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    final msg = context.messages.discovery.card;
    return Card(
      child: ListTile(
        title: Text(msg.title(ip)),
        subtitle: Text(msg.subtitle),
        onTap: onTap,
      ),
    );
  }
}
