import 'dart:async';
import 'dart:io';

import 'package:kiu/bot/bot.dart';
import 'package:kiu/bot/discovery_service.dart';
import 'package:multicast_lock/multicast_lock.dart';
import 'package:udp/udp.dart';

class DiscoveryServiceImpl implements DiscoveryService {
  @override
  Stream<Bot> findBots() {
    final controller = StreamController<Bot>();
    _detect(controller);
    return controller.stream;
  }

  Future<void> _detect(StreamController<Bot> controller) async {
    final lock = MulticastLock();
    await lock.acquire();
    try {
      final endpoint = Endpoint.multicast(
        InternetAddress(BROADCAST_GROUP),
        port: Port(PORT),
      );

      final receiver = await UDP.bind(endpoint);

      final found = <String>{};
      final listen = receiver.listen(
        (it) {
          final host = it.address.host;
          if (found.add(host)) {
            controller.add(Bot(ip: host));
          }
        },
        timeout: Duration(seconds: 5),
      );

      await Future.any([
        controller.done,
        listen,
      ]);

      receiver.close();
    } finally {
      await controller.close();
      await lock.release();
    }
  }
}
