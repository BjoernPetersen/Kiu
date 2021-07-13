import 'dart:async';

import 'package:kiu/bot/state/error_state.dart';
import 'package:kiu/data/action_error.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';

class ErrorAware extends StatefulWidget {
  final Widget child;

  const ErrorAware({required this.child}) : super();

  @override
  _ErrorAwareState createState() => _ErrorAwareState();
}

class _ErrorAwareState extends State<ErrorAware> {
  late StreamSubscription<ActionError?> _sub;

  @override
  void initState() {
    super.initState();

    final errorState = service<ErrorState>();
    _sub = errorState.stream.listen((event) {
      if (event != null && this.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(event.toSnackBar(context));
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return widget.child;
  }

  @override
  void dispose() {
    _sub.cancel();
    super.dispose();
  }
}
