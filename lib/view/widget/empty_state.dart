import 'package:flutter/material.dart';

class EmptyState extends StatelessWidget {
  final Image? image;
  final String text;

  const EmptyState({
    this.image,
    required this.text,
  }) : super();

  @override
  Widget build(BuildContext context) {
    final image = this.image;
    return Center(
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          if (image != null) image,
          Text(text),
        ],
      ),
    );
  }
}
