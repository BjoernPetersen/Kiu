import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/material.dart';
import 'package:kiu/bot/bot_service.dart';
import 'package:kiu/bot/model.dart';
import 'package:kiu/bot/state/reporting_wrapper.dart';

class Bot {
  final String ip;
  Future<BotInfo> _version;

  Future<BotInfo> get version {
    if (_version == null) {
      final version = _loadVersion().catchError((e) async {
        print(e);
        _version = null;
        throw e;
      });
      _version = version;
      return version;
    } else {
      return _version;
    }
  }

  Future<BotInfo> _loadVersion() async {
    return await createService().getVersion();
  }

  String get _baseUrl {
    return "http://$ip:42945";
  }

  String albumArtUrl(Song song) {
    final baseUrl = _baseUrl;
    final path = song.albumArtPath;
    if (baseUrl == null || path == null) return null;
    return "$baseUrl$path";
  }

  Bot({@required this.ip});

  BotService createService([String authorization]) {
    final options = BaseOptions(
      headers: _createHeaders(authorization),
      connectTimeout: 4000,
    );
    final botService = BotService(Dio(options), _baseUrl);
    return ReportingWrapper(botService);
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
