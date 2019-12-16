import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/permission.dart';

abstract class ConnectionManager {
  bool hasPermission(Permission permission);

  bool hasBot();

  void reset();

  Future<BotService> getService();
}
