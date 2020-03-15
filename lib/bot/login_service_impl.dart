import 'dart:convert';
import 'dart:io';

import 'package:dio/dio.dart';
import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/login_service.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/data/preferences.dart';

class LoginServiceImpl implements LoginService {
  BotService _createService() {
    final _baseUrl = baseUrl;
    if (_baseUrl == null) {
      throw MissingBotException();
    }
    final options = BaseOptions(connectTimeout: 4000);
    return BotService(Dio(options), _baseUrl);
  }

  String basicHeader(String username, String password) {
    final input = "$username:$password";
    final bytes = Utf8Encoder().convert(input);
    final encoded = Base64Encoder().convert(bytes);
    return "Basic $encoded";
  }

  void checkIo(DioError e) {
    switch (e.type) {
      case DioErrorType.CONNECT_TIMEOUT:
      case DioErrorType.SEND_TIMEOUT:
      case DioErrorType.RECEIVE_TIMEOUT:
      case DioErrorType.DEFAULT:
        throw IOException();
      default:
        return;
    }
  }

  @override
  Future<Tokens> login(String username, [String password]) async {
    final instanceId = Preference.install_id.getString();
    final passwords = password == null ? [instanceId] : [password, instanceId];
    final service = _createService();
    for (final password in passwords) {
      try {
        return await service.login(basicHeader(username, password));
      } on DioError catch (e) {
        checkIo(e);
        switch (e.response.statusCode) {
          case 404:
            return await register(username);
          default:
            continue;
        }
      }
    }

    if (password == null) {
      throw MissingPasswordException();
    } else {
      throw WrongCredentialsException();
    }
  }

  @override
  Future<Tokens> refresh() async {
    final token = Preference.refresh_token.getString();
    if (token == null) throw WrongCredentialsException();
    final service = _createService();
    try {
      return await service.refresh("Bearer $token");
    } on DioError catch (e) {
      checkIo(e);
      switch (e.response.statusCode) {
        case 401:
          throw WrongCredentialsException();
        default:
          throw IOException();
      }
    }
  }

  @override
  Future<Tokens> register(String username) async {
    final credentials = RegisterCredentials(
      name: username,
      userId: Preference.install_id.getString(),
    );
    try {
      return await _createService().register(credentials);
    } on DioError catch (e) {
      checkIo(e);
      if (e.response.statusCode != 409) {
        throw StateError('Unknown status code: ${e.response.statusCode}');
      }
      throw ConflictException();
    }
  }
}
