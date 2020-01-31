import 'package:flutter/material.dart';

class UnanimatedRoute<T> extends MaterialPageRoute<T> {
  UnanimatedRoute(WidgetBuilder builder) : super(builder: builder);

  @override
  Widget buildTransitions(
    BuildContext context,
    Animation<double> animation,
    Animation<double> secondaryAnimation,
    Widget child,
  ) {
    return child;
  }
}
