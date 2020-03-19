import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/permission.dart';

abstract class AccessManager implements PermissionOwner {
  addListener(Function() listener);

  removeListener(Function() listener);

  Future<BotService> createService();

  reset();
}

abstract class PermissionOwner {
  bool hasPermission(Permission permission);
}

abstract class RefreshTokenException implements Exception {}

class MissingBotException extends RefreshTokenException {}

class InvalidRefreshTokenException extends RefreshTokenException {}

class MissingRefreshTokenException extends RefreshTokenException {}
