import 'package:kiu/view/common.dart';

class LifecycleListener extends StatefulWidget {
  final Widget child;
  final void Function()? onResume;
  final void Function()? onPause;

  const LifecycleListener({
    required this.child,
    this.onResume,
    this.onPause,
  }) : super();

  @override
  _LifecycleListenerState createState() => _LifecycleListenerState();
}

class _LifecycleListenerState extends State<LifecycleListener>
    with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance!.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance!.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.resumed:
        final onResume = widget.onResume;
        if (onResume != null) {
          onResume();
        }
        break;
      case AppLifecycleState.paused:
        final onPause = widget.onPause;
        if (onPause != null) {
          onPause();
        }
        break;
      default:
        break;
    }
  }

  @override
  Widget build(BuildContext context) {
    return widget.child;
  }
}
