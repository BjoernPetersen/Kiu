package com.github.bjoernpetersen.q.api

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object Config {
  private lateinit var prefs: SharedPreferences

  enum class Key(val key: String) {
    HOST("host"), USERNAME("userName"), PASSWORD("password"),
    TOKEN("token"), TOKEN_TYPE("token_type")
  }

  fun init(ctx: Context) {
    this.prefs = PreferenceManager.getDefaultSharedPreferences(ctx)
  }

  var host: String
    get() = prefs.getString(Key.HOST, "localhost")
    set(value) {
      persist(Key.HOST, value)
      Connection.basePath = basePath
    }

  var user: String
    get() {
      val value: String? = prefs.getString(Key.USERNAME, null)
      if (value == null) throw IllegalStateException()
      else return value
    }
    set(value) {
      persist(Key.USERNAME, value)
      Auth.clear()
    }

  fun hasUser(): Boolean = prefs.contains(Key.USERNAME)

  var password: String
    get() {
      val value: String? = prefs.getString(Key.PASSWORD, null)
      if (value == null) throw IllegalStateException()
      else return value
    }
    set(value) {
      persist(Key.PASSWORD, value)
      Auth.clear()
    }

  fun hasPassword(): Boolean = prefs.contains(Key.PASSWORD)
  fun clearPassword() = prefs.edit().remove(Key.PASSWORD).apply()

  var apiKey: ApiKey?
    get() {
      val rawToken: String = prefs.getString(Key.TOKEN, null) ?: return null
      val tokenType: Boolean = prefs.getBoolean(Key.TOKEN_TYPE, false)
      return try {
        if (tokenType) ApiKey.full(rawToken)
        else ApiKey.guest(rawToken)
      } catch (e: InvalidApiKeyException) {
        null
      }
    }
    set(value) {
      persist(Key.TOKEN, value?.raw)
      persist(Key.TOKEN_TYPE,
          if (value == null) null
          else value.userType == ApiKey.UserType.FULL
      )
    }

  val basePath: String
    get() = "http://$host:42945/v1"

  fun reset() {
    Key.values().filter { it != Key.HOST }.forEach { remove(it) }
    Auth.clear()
  }

  private fun remove(key: Key) {
    prefs.edit().remove(key.key).apply()
  }

  private fun persist(key: Key, value: String?) {
    if (value == null) remove(key)
    else prefs.edit().putString(key, value).apply()
  }

  private fun persist(key: Key, value: Boolean?) {
    if (value == null) remove(key)
    else prefs.edit().putBoolean(key, value).apply()
  }
}

private fun SharedPreferences.contains(key: Config.Key) = this.contains(key.key)

private fun SharedPreferences.getString(key: Config.Key, default: String?) =
    this.getString(key.key, default)

private fun SharedPreferences.getBoolean(key: Config.Key, default: Boolean) =
    this.getBoolean(key.key, default)

private fun SharedPreferences.Editor.putString(key: Config.Key, value: String) =
    this.putString(key.key, value)

private fun SharedPreferences.Editor.putBoolean(key: Config.Key, value: Boolean) =
    this.putBoolean(key.key, value)

private fun SharedPreferences.Editor.remove(key: Config.Key) = this.remove(key.key)