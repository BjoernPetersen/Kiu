import 'package:flutter/material.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';
import 'package:kiu/view/page/bot_page.dart';
import 'package:kiu/view/page/login_page.dart';
import 'package:kiu/view/page/queue_page.dart';

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await DependencyModel().init();
  runApp(Kiu());
}

class Kiu extends StatelessWidget {
  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Kiu',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primaryColor: Color(0xFFBBC0CA),
      ),
      initialRoute: _initialRoute(),
      routes: {
        "/queue": (_) => QueuePage(),
        "/selectBot": (_) => BotPage(),
        "/login": (_) => LoginPage(),
      },
    );
  }
}

String _initialRoute() {
  if (Preference.username == null) {
    return "/login";
  } else {
    return "/queue";
  }
}
