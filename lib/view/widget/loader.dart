import 'package:flutter/cupertino.dart';
import 'package:flutter/material.dart';

class Loader extends StatelessWidget {
  final String text;

  const Loader({Key key, this.text}) : super(key: key);

  @override
  Widget build(BuildContext context) => Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: _createChildren(),
        ),
      );

  List<Widget> _createChildren() {
    final loader = Container(
      height: 48,
      width: 48,
      child: CircularProgressIndicator(),
    );

    if (text == null) {
      return [loader];
    } else {
      return [
        loader,
        Container(height: 15),
        Text(text),
      ];
    }
  }
}
