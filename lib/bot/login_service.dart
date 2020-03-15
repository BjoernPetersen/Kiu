import 'package:kiu/bot/model.dart';

abstract class LoginService {
  Future<Tokens> login(String username, [String password]);

  Future<Tokens> refresh();

  Future<Tokens> register(String username);
}

abstract class LoginException implements Exception {}

class IOException extends LoginException {}

class MissingBotException extends LoginException {}

class WrongCredentialsException extends LoginException {}

class MissingPasswordException extends LoginException {}

class ConflictException extends LoginException {}
