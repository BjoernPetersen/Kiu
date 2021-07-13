import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:kiu/view/resources/messages.i18n.dart';
import 'package:kiu/view/resources/messages_de.i18n.dart';
import 'package:provider/provider.dart';

class MessagesProvider extends StatelessWidget {
  final Widget child;

  const MessagesProvider({required this.child}) : super();

  @override
  Widget build(BuildContext context) {
    print(_createMessages(context));
    return Provider(
      create: (context) => _createMessages(context),
      child: child,
    );
  }

  Messages _createMessages(BuildContext context) {
    final languageCode = window.locale.languageCode;
    switch (languageCode) {
      case 'de':
        return MessagesDe();
      default:
        return Messages();
    }
  }
}
