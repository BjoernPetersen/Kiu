import 'package:kiu/view/common.dart';

class RetryContent extends StatelessWidget {
  final String text;
  final Function() refresh;

  const RetryContent({
    Key key,
    @required this.text,
    @required this.refresh,
  }) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: <Widget>[
          Text(text),
          Row(
            mainAxisSize: MainAxisSize.min,
            children: <Widget>[
              IconButton(
                icon: Icon(Icons.refresh),
                onPressed: refresh,
                tooltip: context.messages.common.retry,
              ),
            ],
          ),
        ],
      ),
    );
  }
}
