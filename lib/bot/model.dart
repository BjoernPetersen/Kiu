import 'package:json_annotation/json_annotation.dart';

part 'model.g.dart';

@JsonSerializable()
class Tokens {
  final String accessToken;
  final String? refreshToken;

  Tokens({required this.accessToken, this.refreshToken});

  factory Tokens.fromJson(Map<String, dynamic> json) => _$TokensFromJson(json);
}

@JsonSerializable()
class ImplementationInfo {
  final String name;
  final String version;
  final String projectInfo;

  ImplementationInfo({
    required this.name,
    required this.version,
    required this.projectInfo,
  });

  factory ImplementationInfo.fromJson(Map<String, dynamic> json) =>
      _$ImplementationInfoFromJson(json);
}

@JsonSerializable()
class BotInfo {
  final String apiVersion;
  final String botName;
  final ImplementationInfo implementation;

  BotInfo(
      {required this.apiVersion,
      required this.botName,
      required this.implementation});

  factory BotInfo.fromJson(Map<String, dynamic> json) =>
      _$BotInfoFromJson(json);
}

@JsonSerializable()
class RegisterCredentials {
  final String name;
  final String userId;

  RegisterCredentials({required this.name, required this.userId});

  factory RegisterCredentials.fromJson(Map<String, dynamic> json) =>
      _$RegisterCredentialsFromJson(json);

  Map<String, dynamic> toJson() => _$RegisterCredentialsToJson(this);
}

@JsonSerializable()
class PasswordChange {
  final String newPassword;

  PasswordChange({required this.newPassword});

  Map<String, dynamic> toJson() => _$PasswordChangeToJson(this);
}

@JsonSerializable()
class NamedPlugin {
  final String id;
  final String name;

  NamedPlugin({required this.id, required this.name});

  factory NamedPlugin.fromJson(Map<String, dynamic> json) =>
      _$NamedPluginFromJson(json);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is NamedPlugin &&
          runtimeType == other.runtimeType &&
          id == other.id;

  @override
  int get hashCode => id.hashCode;
}

@JsonSerializable()
class Song {
  final String id;
  final NamedPlugin provider;
  final String title;
  final String description;
  final int? duration;
  final String? albumArtPath;

  Song({
    required this.id,
    required this.provider,
    required this.title,
    required this.description,
    required this.duration,
    required this.albumArtPath,
  });

  factory Song.fromJson(Map<String, dynamic> json) => _$SongFromJson(json);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is Song &&
          runtimeType == other.runtimeType &&
          id == other.id &&
          provider == other.provider;

  @override
  int get hashCode => id.hashCode ^ provider.hashCode;
}

@JsonSerializable()
class SongEntry {
  final Song song;
  final String? userName;

  SongEntry({required this.song, required this.userName});

  factory SongEntry.fromJson(Map<String, dynamic> json) =>
      _$SongEntryFromJson(json);

  @override
  bool operator ==(Object other) =>
      identical(this, other) ||
      other is SongEntry &&
          runtimeType == other.runtimeType &&
          song == other.song &&
          userName == other.userName;

  @override
  int get hashCode => song.hashCode ^ userName.hashCode;
}

@JsonSerializable()
class Volume {
  final int volume;
  final bool isSupported;

  Volume({required this.volume, required this.isSupported});

  Volume.unsupported() : this(volume: 100, isSupported: false);

  Volume.supported(int value) : this(volume: value, isSupported: true);

  factory Volume.fromJson(Map<String, dynamic> json) => _$VolumeFromJson(json);
}

@JsonSerializable()
class PlayerState {
  @JsonKey(fromJson: _stateFromJson)
  final PlayerStateType state;
  final SongEntry? songEntry;
  final int? progress;

  PlayerState({
    required this.state,
    required this.songEntry,
    required this.progress,
  });

  factory PlayerState.fromJson(Map<String, dynamic> json) =>
      _$PlayerStateFromJson(json);
}

enum PlayerStateType { play, pause, stop, error }

PlayerStateType _stateFromJson(dynamic value) {
  switch (value) {
    case "PLAY":
      return PlayerStateType.play;
    case "PAUSE":
      return PlayerStateType.pause;
    case "STOP":
      return PlayerStateType.stop;
    case "ERROR":
    default:
      return PlayerStateType.error;
  }
}

@JsonSerializable()
class PlayerStateChange {
  @JsonKey(toJson: _stateChangeToJson)
  final PlayerStateChangeAction action;

  PlayerStateChange(this.action);

  Map<String, dynamic> toJson() {
    return _$PlayerStateChangeToJson(this);
  }
}

enum PlayerStateChangeAction { play, pause, skip }

extension on PlayerStateChangeAction {
  String get text {
    switch (this) {
      case PlayerStateChangeAction.play:
        return "PLAY";
      case PlayerStateChangeAction.pause:
        return "PAUSE";
      case PlayerStateChangeAction.skip:
        return "SKIP";
    }
  }
}

String _stateChangeToJson(PlayerStateChangeAction action) {
  return action.text;
}
