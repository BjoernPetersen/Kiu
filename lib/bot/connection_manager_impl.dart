import 'package:corsac_jwt/corsac_jwt.dart';
import 'package:dio/dio.dart';
import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/permission.dart';

class ConnectionManagerImpl implements ConnectionManager {
  BotService _service;
  Token _token;

  @override
  bool hasPermission(Permission permission) =>
      _token?.permissions?.contains(permission);

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
  bool hasBot() => baseUrl != null;

  @override
  void reset() {
    this._service = null;
  }

  Future<BotService> _createService() async {
    final _baseUrl = baseUrl;
    if (_baseUrl == null) {
      throw StateError('No bot selected');
    }
    final token = _token ?? await _refreshToken();

    final options = BaseOptions(headers: {"Authorization": "Bearer $token"});
    return BotService(Dio(options), _baseUrl);
  }

  Future<Token> _refreshToken() async {
    // TODO refresh
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
    final List<String> permissions = parsed.getClaim("permissions");
    final parsedPermissions = permissions.map(parsePermission).toSet();
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
