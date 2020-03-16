import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/model.dart';

class Bot {
  final String ip;
  final BotInfo version;

  String get _baseUrl {
    return "http://$ip:42945";
  }

  Bot({this.version, @required this.ip});

  BotService createService([String authorization]) {
    final options = BaseOptions(
      headers: _createHeaders(authorization),
      connectTimeout: 4000,
    );
    return BotService(Dio(options), _baseUrl);
  }

  _createHeaders(String authorization) {
    if (authorization == null) {
      return null;
    } else {
      return {
        "Authorization": authorization,
      };
    }
  }
}

String bearerAuth(String token) {
  return "Bearer $token";
}

String basicAuth(String username, String password) {
  final input = "$username:$password";
  final bytes = Utf8Encoder().convert(input);
  final encoded = Base64Encoder().convert(bytes);
  return "Basic $encoded";
}
