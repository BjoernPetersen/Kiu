import 'package:flutter/material.dart';

class SaveTab extends StatelessWidget {
  final Function(int) save;
  final Widget child;

  const SaveTab({required this.save, required this.child}) : super();

  @override
  Widget build(BuildContext context) {
    final controller = DefaultTabController.of(context)!;
    controller.addListener(() => save(controller.index));
    return child;
  }
}
