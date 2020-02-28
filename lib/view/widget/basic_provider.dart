import 'package:flutter/material.dart';
import 'package:kiu/view/widget/messages_provider.dart';

class BasicProvider extends StatelessWidget {
  final Widget child;

  const BasicProvider({Key key, @required this.child}) : super(key: key);

  @override
  Widget build(BuildContext context) => MessagesProvider(child: child);
}
