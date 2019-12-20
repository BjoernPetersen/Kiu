import 'dart:async';

import 'package:corsac_jwt/corsac_jwt.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/login_service.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';

class ConnectionManagerImpl implements ConnectionManager {
  BotService _service;
  final ValueNotifier<Token> _token = ValueNotifier(null);

  ConnectionManagerImpl() {
    _token.addListener(() => Preference.token.setString(_token.value?.value));
  }

  @override
  bool hasPermission(Permission permission) =>
      _token.value?.permissions?.contains(permission) ?? false;

  @override
  Future<BotService> getService() async {
    final service = _service;
    if (service == null) {
      return await _createService();
    } else {
      return service;
    }
  }

  @override
  addTokenListener(Function() listener) {
    _token.addListener(listener);
  }

  @override
  removeTokenListener(Function() listener) {
    _token.removeListener(listener);
  }

  @override
  bool hasBot() => baseUrl != null;

  @override
  void reset() {
    this._service = null;
    this._token.value = null;
  }

  Future<BotService> _createService() async {
    final _baseUrl = baseUrl;
    if (_baseUrl == null) {
      throw StateError('No bot selected');
    }
    final token = _token.value ?? await _refreshToken();

    final options = BaseOptions(
      headers: {"Authorization": "Bearer $token"},
      connectTimeout: 4000,
    );
    return BotService(Dio(options), _baseUrl);
  }

  Future<Token> _refreshToken() async {
    final value = await service<LoginService>().login(
      Preference.username.getString(),
      Preference.password.getString(),
    );
    final token = Token.fromValue(value);
    _token.value = token;
    return token;
  }
}

class Token {
  final String value;
  final DateTime expiration;
  final Set<Permission> permissions;

  Token(this.value, this.expiration, this.permissions);

  factory Token.fromValue(String value) {
    final parsed = JWT.parse(value);
    final expiration =
        DateTime.fromMillisecondsSinceEpoch(parsed.expiresAt * 1000);
    final List<dynamic> permissions = parsed.getClaim("permissions");
    final parsedPermissions =
        permissions.map(parsePermission).where((it) => it != null).toSet();
    return Token(value, expiration, parsedPermissions);
  }

  bool isExpired() {
    return DateTime.now().isAfter(expiration);
  }

  @override
  String toString() {
    return value;
  }
}
