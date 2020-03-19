import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/permission.dart';

@Deprecated("For removal")
abstract class ConnectionManager implements PermissionHolder {
  addTokenListener(Function() listener);

  removeTokenListener(Function() listener);

  bool hasBot();

  void reset();

  Future<BotService> getService();
}

@Deprecated("For removal")
abstract class PermissionHolder {
  bool hasPermission(Permission permission);
}
