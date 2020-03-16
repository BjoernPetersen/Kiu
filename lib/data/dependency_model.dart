import 'package:get_it/get_it.dart';
import 'package:kiu/bot/connection_manager.dart';
import 'package:kiu/bot/connection_manager_impl.dart';
import 'package:kiu/bot/discovery_service.dart';
import 'package:kiu/bot/discovery_service_impl.dart';
import 'package:kiu/bot/login_service.dart';
import 'package:kiu/bot/login_service_impl.dart';
import 'package:kiu/bot/state/bot_connection.dart';
import 'package:kiu/bot/state/error_state.dart';
import 'package:kiu/bot/state/state_manager.dart';
import 'package:kiu/bot/state/state_manager_impl.dart';
import 'package:kiu/data/preferences.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:uuid/uuid.dart';
import 'package:uuid/uuid_util.dart';

final service = GetIt.instance;

class DependencyModel {
  Future<void> init() async {
    final prefs = await SharedPreferences.getInstance();
    service.registerSingleton<SharedPreferences>(prefs);
    service.registerSingleton<LoginService>(LoginServiceImpl());
    service.registerSingleton<DiscoveryService>(DiscoveryServiceImpl());
    service.registerSingleton<ConnectionManager>(ConnectionManagerImpl());
    service.registerSingleton<StateManager>(
      StateManagerImpl(service<ConnectionManager>()),
    );
    service.registerSingleton(ErrorState());
    service.registerSingleton(BotConnection());
    final instanceId = Preference.install_id.getString();
    if (instanceId == null) {
      final uuid = Uuid(options: {'grng': UuidUtil.cryptoRNG});
      await Preference.install_id.setString(uuid.v4());
    }
  }
}
