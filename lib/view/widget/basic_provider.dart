import 'package:flutter/material.dart';
import 'package:kiu/view/widget/messages_provider.dart';

class BasicProvider extends StatelessWidget {
  final Widget child;

  const BasicProvider({required this.child}) : super();

  @override
  Widget build(BuildContext context) => MessagesProvider(child: child);
}
