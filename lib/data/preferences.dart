import 'package:kiu/data/dependency_model.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum Preference {
  install_id,
  username,
  refresh_token,
  bot_ip,
  suggester_id,
  provider_id,
}

extension PreferenceAccess on Preference {
  // ignore: missing_return
  String get key {
    switch (this) {
      case Preference.install_id:
        return "install_id";
      case Preference.username:
        return "username";
      case Preference.refresh_token:
        return "rrefresh_token";
      case Preference.bot_ip:
        return "bot_ip";
      case Preference.suggester_id:
        return "suggester_id";
      case Preference.provider_id:
        return "provider_id";
    }
  }

  SharedPreferences _prefs() => service<SharedPreferences>();

  String getString() => _prefs().getString(key);

  Future<bool> remove() => _prefs().remove(key);

  Future<bool> setString(String value) => _prefs().setString(key, value);
}
