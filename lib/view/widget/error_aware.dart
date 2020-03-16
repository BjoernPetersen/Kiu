import 'dart:async';

import 'package:kiu/bot/state/error_state.dart';
import 'package:kiu/data/action_error.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/view/common.dart';

class ErrorAware extends StatefulWidget {
  final Widget child;

  const ErrorAware({Key key, this.child}) : super(key: key);

  @override
  _ErrorAwareState createState() => _ErrorAwareState();
}

class _ErrorAwareState extends State<ErrorAware> {
  StreamSubscription<ActionError> _sub;
  ActionError _initError;

  @override
  void initState() {
    super.initState();

    final errorState = service<ErrorState>();
    _initError = errorState.lastValue;
    errorState.stream.listen((event) {
      if (event != null && this.mounted) {
        Scaffold.of(context).showSnackBar(event.toSnackBar(context));
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
