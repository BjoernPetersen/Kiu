import 'package:flutter/material.dart';

class EmptyState extends StatelessWidget {
  final Image image;
  final String text;

  const EmptyState({
    Key key,
    this.image,
    @required this.text,
  }) : super(key: key);

  List<Widget> createChildren() {
    final message = Text(text);
    if (image != null) {
      return [image, message];
    } else {
      return [message];
    }
  }

  @override
  Widget build(BuildContext context) => Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: createChildren(),
        ),
      );
}
