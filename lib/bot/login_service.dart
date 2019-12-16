abstract class LoginService {
  Future<String> login(String username, [String password]);

  Future<String> register(String username);
}

abstract class LoginException implements Exception {}

class IOException extends LoginException {}

class MissingBotException extends LoginException {}

class WrongCredentialsException extends LoginException {}

class MissingPasswordException extends LoginException {}

class ConflictException extends LoginException {}
