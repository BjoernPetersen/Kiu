import 'dart:async';

import 'package:flutter/material.dart';
import 'package:fluttertoast/fluttertoast.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/state_manager.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/data/sharing_data.dart';
import 'package:kiu/view/page/bot_page.dart';
import 'package:kiu/view/page/login_page.dart';
import 'package:kiu/view/page/queue_page.dart';
import 'package:kiu/view/page/state_page.dart';
import 'package:kiu/view/page/suggestions_page.dart';
import 'package:receive_sharing_intent/receive_sharing_intent.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await DependencyModel().init();
  runApp(Kiu());
}

class Kiu extends StatefulWidget {
  @override
  _KiuState createState() => _KiuState();
}

class _KiuState extends State<Kiu> {
  bool _isHandlingShares = false;
  StreamSubscription _shareSub;

  Future<void> _handleShare(String share) async {
    if (share == null) return;
    final parsed = extractSharingData(share);
    if (parsed == null) return;
    try {
      final bot = await service<ConnectionManager>().getService();
      final queue = await bot.enqueue(parsed.songId, parsed.providerId);
      service<StateManager>().updateQueue(queue);
    } catch (err) {
      await Fluttertoast.showToast(msg: "Could not enqueue shared song");
    }
  }

  _handleShares() {
    _isHandlingShares = true;
    ReceiveSharingIntent.getInitialText().then((it) {
      _handleShare(it);
      ReceiveSharingIntent.reset();
    });
    _shareSub = ReceiveSharingIntent.getTextStream().listen(_handleShare);
  }

  @override
  void dispose() {
    _shareSub.cancel();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    if (!_isHandlingShares) {
      _handleShares();
    }
    return MaterialApp(
      title: 'Kiu',
      theme: ThemeData(
        primaryColor: Color(0xFFBBC0CA),
        highlightColor: Color(0xFFB0B5C1),
      ),
      initialRoute: _initialRoute(),
      routes: {
        "/selectBot": (_) => BotPage(),
        "/login": (_) => LoginPage(),
        "/queue": (_) => QueuePage(),
        "/suggestions": (_) => SuggestionsPage(),
        "/state": (_) => StatePage(),
      },
    );
  }
}

String _initialRoute() {
  final username = Preference.username.getString();
  if (username == null || username.isEmpty) {
    return "/login";
  } else {
    return "/queue";
  }
}
