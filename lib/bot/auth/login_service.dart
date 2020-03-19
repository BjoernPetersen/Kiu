import 'package:kiu/data/preferences.dart';

class LoginService {

}

abstract class LoginException implements Exception {}

class IOException extends LoginException {}

class MissingBotException extends LoginException {}

class WrongCredentialsException extends LoginException {}

class MissingPasswordException extends LoginException {}

class ConflictException extends LoginException {}
