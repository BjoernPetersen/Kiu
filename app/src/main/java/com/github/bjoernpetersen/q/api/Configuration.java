package com.github.bjoernpetersen.q.api;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import com.github.bjoernpetersen.q.api.ApiKey.UserType;
import com.hadisatrio.optional.Optional;

public final class Configuration {

  private static final String HOST_KEY = "host";

  private static final String USERNAME_KEY = "userName";
  private static final String PASSWORD_KEY = "password";
  private static final String TOKEN_KEY = "token";
  private static final String TOKEN_TYPE = "token_type";

  @NonNull
  private final SharedPreferences preferences;

  public Configuration(@NonNull SharedPreferences preferences) {
    this.preferences = preferences;
  }

  void updateApiKey(@NonNull ApiKey apiKey) {
    preferences.edit()
        .putString(TOKEN_KEY, apiKey.getRaw())
        .putBoolean(TOKEN_TYPE, apiKey.getUserType() == UserType.FULL)
        .apply();
  }

  void setUserName(@NonNull String userName) {
    userName = userName.trim();
    preferences.edit()
        .putString(USERNAME_KEY, userName)
        .apply();
  }

  public boolean hasUserName() {
    return getUserName().isPresent();
  }

  public void setHost(@NonNull String host) {
    preferences.edit()
        .putString(HOST_KEY, host)
        .apply();
  }

  void reset() {
    preferences.edit()
        .remove(USERNAME_KEY)
        .remove(PASSWORD_KEY)
        .remove(TOKEN_KEY)
        .remove(TOKEN_TYPE)
        .apply();
  }

  /**
   * Sets a password. This should only be used before logging in / registering. Afterwards use
   * {@link Connection#upgrade(String)}.
   *
   * @param password a password
   */
  public void setPassword(@NonNull String password) {
    password = password.trim();
    preferences.edit()
        .putString(PASSWORD_KEY, password)
        .apply();
  }

  String getBasePath() {
    return "http://" + getHost() + ":4567/v1";
  }

  @NonNull
  private String getHost() {
    // TODO replace default with localhost
    return preferences.getString(HOST_KEY, "192.168.0.11");
  }

  Optional<ApiKey> getApiKey() {
    String token = preferences.getString(TOKEN_KEY, null);
    if (token == null) {
      return Optional.absent();
    }
    boolean tokenIsFull = preferences.getBoolean(TOKEN_TYPE, false);
    try {
      if (tokenIsFull) {
        return Optional.of(ApiKey.full(token));
      } else {
        return Optional.of(ApiKey.guest(token));
      }
    } catch (InvalidApiKeyException e) {
      Log.w(getClass().getName(), "Invalid API key loaded");
      return Optional.absent();
    }
  }

  Optional<String> getUserName() {
    return Optional.ofNullable(preferences.getString(USERNAME_KEY, null));
  }

  Optional<String> getPassword() {
    return Optional.ofNullable(preferences.getString(PASSWORD_KEY, null));
  }
}
