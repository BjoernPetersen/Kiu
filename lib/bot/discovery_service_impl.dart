import 'dart:io';

import 'package:kiu/bot/discovery_service.dart';
import 'package:multicast_lock/multicast_lock.dart';
import 'package:udp/udp.dart';

class DiscoveryServiceImpl implements DiscoveryService {
  @override
  Future<Iterable<String>> findBots() async {
    final lock = MulticastLock();
    await lock.acquire();
    try {
      final endpoint = Endpoint.multicast(
        InternetAddress(BROADCAST_GROUP),
        port: Port(PORT),
      );

      final receiver = await UDP.bind(endpoint);

      final found = <String>{};
      await receiver.listen(
        (it) => found.add(it.address.host),
        timeout: Duration(seconds: 3),
      );

      receiver.close();
      return found;
    } finally {
      await lock.release();
    }
  }
}
