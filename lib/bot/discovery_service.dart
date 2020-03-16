import 'package:kiu/bot/bot.dart';

const String BROADCAST_GROUP = '224.0.0.142';
const int PORT = 42945;

abstract class DiscoveryService {
  Stream<Bot> findBots();
}
