import 'package:flutter/material.dart';
import 'package:kiu/view/resources/messages.i18n.dart';
import 'package:provider/provider.dart';

export 'package:flutter/material.dart';
export 'package:provider/provider.dart';

extension MessagesAccess on BuildContext {
  Messages get messages => Provider.of<Messages>(this, listen: false);
}
