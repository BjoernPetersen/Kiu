import 'package:kiu/data/dependency_model.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum Preference {
  install_id,
  username,
  password,
  token,
  bot_ip,
}

extension PreferenceAccess on Preference {
  // ignore: missing_return
  String get key {
    switch (this) {
      case Preference.install_id:
        return "install_id";
      case Preference.username:
        return "username";
      case Preference.password:
        return "password";
      case Preference.token:
        return "token";
      case Preference.bot_ip:
        return "bot_ip";
    }
  }

  SharedPreferences _prefs() => service<SharedPreferences>();

  String getString() => _prefs().getString(key);

  void remove() => _prefs().remove(key);

  Future<bool> setString(String value) => _prefs().setString(key, value);
}
