import 'dart:async';

import 'package:flutter/material.dart';
import 'package:kiu/bot/bot.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/discovery_service.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';

class DiscoveryController {
  Function onUpdate = () => {};
  bool _isLoading = false;
  List<Bot> _found = [];
  StreamSubscription _sub;

  bool get isLoading => _isLoading;

  List<Bot> get found => _found;

  void refresh() {
    _sub?.cancel();
    _isLoading = true;
    _found = [];
    onUpdate();
    _sub = service<DiscoveryService>().findBots().listen((event) {
      _found.add(event);
      onUpdate();
    }, onDone: () {
      _isLoading = false;
      onUpdate();
    });
  }

  void dispose() {
    _sub.cancel();
  }

  Future<void> setIp(NavigatorState nav, String ip) async {
    await Preference.bot_ip.setString(ip);
    service<ConnectionManager>().reset();
    if (nav.canPop()) {
      nav.pop();
    } else {
      nav.pushReplacementNamed("/login");
    }
  }
}
