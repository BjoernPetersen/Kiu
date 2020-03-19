import 'package:corsac_jwt/corsac_jwt.dart';
import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:kiu/bot/auth/access_manager.dart';
import 'package:kiu/bot/auth/credential_manager.dart';
import 'package:kiu/bot/bot.dart';
import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/permission.dart';
import 'package:kiu/bot/state/bot_connection.dart';
import 'package:kiu/data/dependency_model.dart';
import 'package:kiu/data/preferences.dart';

class AccessManagerImpl implements AccessManager {
  final ValueNotifier<_Token> _token = ValueNotifier(null);
  Bot _bot;

  AccessManagerImpl() {
    service<BotConnection>().bot.stream.listen((bot) {
      _bot = bot;
      _token.value = null;
    });
  }

  @override
  addListener(Function() listener) {
    _token.addListener(listener);
  }

  @override
  removeListener(Function() listener) {
    _token.removeListener(listener);
  }

  _Token _processTokens(Bot bot, Tokens tokens) {
    final credentialManager = service<CredentialManager>();
    final newRefreshToken = tokens.refreshToken;
    if (newRefreshToken != null) {
      credentialManager.setRefreshToken(bot, newRefreshToken);
    }
    final token = _Token.fromJwt(tokens.accessToken);
    _token.value = token;
    return token;
  }

  Future<_Token> _retrieveToken(Bot bot) async {
    final token = _token.value;
    if (token != null) {
      return token;
    }
    final credentialManager = service<CredentialManager>();
    final refreshToken = await credentialManager.getRefreshToken(bot);
    if (refreshToken == null) {
      throw MissingRefreshTokenException();
    }

    final loginService = bot.createService();
    try {
      final tokens = await loginService.refresh(bearerAuth(refreshToken));
      return _processTokens(bot, tokens);
    } on DioError catch (e) {
      if (e.type == DioErrorType.RESPONSE) {
        final code = e.response.statusCode;
        if (code < 500 && code >= 400) {
          if (code == 404) {
            return _register(bot);
          }

          throw InvalidRefreshTokenException();
        }
      }
      return null;
    }
  }

  Future<_Token> _register(Bot bot) async {
    final username = Preference.username.getString();
    if (username == null) {
      throw MissingRefreshTokenException();
    }

    final service = bot.createService();
    final credentials = RegisterCredentials(
      name: username,
      userId: Preference.install_id.getString(),
    );
    final tokens = await service.register(credentials);
    return _processTokens(bot, tokens);
  }

  @override
  Future<BotService> createService() async {
    final bot = _bot;
    if (bot == null) {
      throw MissingBotException();
    }
    final token = await _retrieveToken(bot);
    if (token == null) return null;
    return bot.createService(bearerAuth(token.value));
  }

  @override
  bool hasPermission(Permission permission) =>
      _token.value?.hasPermission(permission) ?? false;

  @override
  reset() {
    _token.value = null;
  }
}

class _Token implements PermissionOwner {
  final String value;
  final DateTime expiration;
  final Set<Permission> permissions;

  _Token(this.value, this.expiration, this.permissions);

  factory _Token.fromJwt(String jwt) {
    final parsed = JWT.parse(jwt);
    final expiration =
        DateTime.fromMillisecondsSinceEpoch(parsed.expiresAt * 1000);
    final List<dynamic> permissions = parsed.getClaim("permissions");
    final parsedPermissions =
        permissions.map(parsePermission).where((it) => it != null).toSet();
    return _Token(jwt, expiration, parsedPermissions);
  }

  bool isExpired() {
    return DateTime.now().isAfter(expiration);
  }

  @override
  String toString() {
    return value;
  }

  @override
  bool hasPermission(Permission permission) => permissions.contains(permission);
}
