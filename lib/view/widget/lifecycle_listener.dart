import 'package:kiu/view/common.dart';

class LifecycleListener extends StatefulWidget {
  final Widget child;
  final Function() onResume;
  final Function() onPause;

  const LifecycleListener({
    Key key,
    @required this.child,
    this.onResume,
    this.onPause,
  }) : super(key: key);

  @override
  _LifecycleListenerState createState() => _LifecycleListenerState();
}

class _LifecycleListenerState extends State<LifecycleListener>
    with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.resumed:
        if (widget.onResume != null) {
          widget.onResume();
        }
        break;
      case AppLifecycleState.paused:
        if (widget.onPause != null) {
          widget.onPause();
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
