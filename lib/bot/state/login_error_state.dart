import 'dart:async';

import 'package:kiu/bot/auth/access_manager.dart';

class LoginErrorState {
  final _controller = StreamController<RefreshTokenException>.broadcast();

  Stream<RefreshTokenException> get stream => _controller.stream;

  void update(RefreshTokenException value) {
    _controller.add(value);
  }

  close() {
    _controller.close();
  }
}
