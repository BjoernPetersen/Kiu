import 'package:flutter/material.dart';
import 'package:kiu/view/widget/player_control.dart';

class EmbeddedPlayer extends StatelessWidget {
  final Widget child;

  const EmbeddedPlayer({required this.child}) : super();

  @override
  Widget build(BuildContext context) => Column(
        mainAxisSize: MainAxisSize.max,
        children: <Widget>[Expanded(child: child), PlayerControl()],
      );
}
