import 'package:kiu/bot/model.dart';

@Deprecated("For removal")
abstract class LoginService {
  Future<Tokens> login(String username, [String password]);

  Future<Tokens> refresh();

  Future<Tokens> register(String username);
}

@Deprecated("For removal")
abstract class LoginException implements Exception {}

@Deprecated("For removal")
class IOException extends LoginException {}

@Deprecated("For removal")
class MissingBotException extends LoginException {}

@Deprecated("For removal")
class WrongCredentialsException extends LoginException {}

@Deprecated("For removal")
class MissingPasswordException extends LoginException {}

@Deprecated("For removal")
class ConflictException extends LoginException {}
