import 'package:flutter/material.dart';
import 'package:kiu/data/preferences.dart';

Widget createOverflowItems(BuildContext context, {List<Choice> hidden = const []}) =>
    PopupMenuButton<Choice>(
      itemBuilder: (_) => Choice.values
          .where((it) => !hidden.contains(it))
          .map(
            (it) => PopupMenuItem<Choice>(
              value: it,
              child: Text(it.text),
            ),
          )
          .toList(growable: false),
      onSelected: (choice) {
        final navigator = Navigator.of(context);
        switch (choice) {
          case Choice.choose_bot:
            navigator.pushNamed("/selectBot");
            break;
          case Choice.logout:
            Preference.token.remove();
            Preference.password.remove();
            navigator.pushReplacementNamed("/login");
            break;
        }
      },
      onCanceled: () => print("Cancel"),
    );

enum Choice {
  choose_bot,
  // todo set password
  logout,
}

extension on Choice {
  String get text {
    switch (this) {
      case Choice.choose_bot:
        return 'Switch bot';
      case Choice.logout:
        return 'Log out';
    }
  }
}

_chooseBot() {}
