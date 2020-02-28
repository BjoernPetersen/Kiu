import 'package:flutter/material.dart';
import 'package:kiu/view/resources/messages.i18n.dart';
import 'package:provider/provider.dart';

class BasicProvider extends StatelessWidget {
  final Widget child;

  const BasicProvider({Key key, @required this.child}) : super(key: key);

  @override
  Widget build(BuildContext context) => Provider(
        create: (_) => Messages(),
        child: child,
      );
}
