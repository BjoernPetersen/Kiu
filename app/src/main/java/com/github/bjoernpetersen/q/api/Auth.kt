package com.github.bjoernpetersen.q.api

import android.provider.Settings
import android.util.Log
import com.github.bjoernpetersen.jmusicbot.client.ApiException
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException

private val TAG = "Auth"

internal object Auth {
    private var _apiKey: ApiKey? = null
        set(value) {
            field = value
            Config.apiKey = value
        }

    fun clear() {
        _apiKey = null
    }

    val apiKey: ApiKey
        @Throws(AuthException::class)
        get() {
            var apiKey = _apiKey
            if (apiKey != null && !apiKey.isExpired) {
                return apiKey
            } else {
                apiKey = refreshApiKey()
                _apiKey = apiKey
                return apiKey
            }
        }

    private fun refreshApiKey(): ApiKey {
        try {
            return tryRetrieve()
        } catch (e: ApiException) {
            checkConnectionException(ConnectionException(e), e.cause)
        } catch (e: UnknownAuthException) {
            checkConnectionException(e, e.cause?.cause)
        }
    }

    private fun checkConnectionException(e: Exception, cause: Throwable?): Nothing {
        if (cause is SocketException
                || cause is SocketTimeoutException
                || cause is ConnectException) {
            throw ConnectionException(e)
        }
        throw e
    }

    /**
     * Checks whether there is an API key and the user type is FULL.
     * If there is no API key, this method will not attempt to retrieve one.
     */
    val isFullUser: Boolean
        get() {
            val _apiKey = this._apiKey
            return _apiKey != null && _apiKey.userType == ApiKey.UserType.FULL
        }

    val apiKeyNoRefresh: ApiKey?
        get() = _apiKey

    fun hasPermissionNoRefresh(permission: Permission): Boolean =
            apiKeyNoRefresh?.permissions?.contains(permission) ?: false

    fun hasPermission(permission: Permission): Boolean {
        if (hasPermissionNoRefresh(permission)) return true

        Log.d(TAG, "REFRESHING TOKEN")

        // Check if permission has changed on server side
        try {
            val apiKey = refreshApiKey()
            this._apiKey = apiKey
            return apiKey.permissions.contains(permission)
        } catch (e: AuthException) {
            return false
        }
    }

    private fun tryRetrieve(): ApiKey {
        var apiKey: ApiKey
        // first, try login with existing credentials
        try {
            apiKey = login()
        } catch (e: LoginException) {
            Log.d(TAG, "Login failed", e)
            if (e.reason == LoginException.Reason.WRONG_PASSWORD
                    || e.reason == LoginException.Reason.NEEDS_AUTH) {
                throw e
            }
            // if that didn't work, try to register as a guest
            apiKey = registerGuest()
        }


        // if that did work, try to upgrade to a full user (if a password is present)
        try {
            apiKey = upgrade(apiKey)
        } catch (e: ChangePasswordException) {
            // if this does not work, ignore it
        }
        return apiKey
    }

    /**
     * @throws RegisterException if registering didn't work
     */
    private fun registerGuest(): ApiKey {
        if (!Config.hasUser()) {
            throw RegisterException(RegisterException.Reason.NO_USER)
        }

        val user = Config.user
        val rawToken = try {
            Connection.registerUser(user, Settings.Secure.ANDROID_ID)
        } catch (e: ApiException) {
            when (e.code) {
                409 -> throw RegisterException(RegisterException.Reason.TAKEN)
                else -> throw UnknownAuthException(e)
            }
        }
        return ApiKey.guest(rawToken)
    }

    /**
     * Tries to upgrade the guest user to a full user, if a password is present.
     * @throws ChangePasswordException
     */
    private fun upgrade(apiKey: ApiKey): ApiKey {
        if (!Config.hasPassword()) {
            throw ChangePasswordException(ChangePasswordException.Reason.NO_PASSWORD)
        }
        val password = Config.password
        return changePassword(apiKey, password, null)
    }

    private fun changePassword(apiKey: ApiKey, password: String, oldPassword: String?): ApiKey {
        val rawToken = try {
            Connection.changePassword(apiKey.raw, password, null)
        } catch (e: ApiException) {
            when (e.code) {
                400 -> throw ChangePasswordException(ChangePasswordException.Reason.INVALID_PASSWORD)
                401 -> throw ChangePasswordException(ChangePasswordException.Reason.INVALID_TOKEN)
                403 -> throw ChangePasswordException(ChangePasswordException.Reason.WRONG_OLD_PASSWORD)
                else -> throw UnknownAuthException(e)
            }
        }
        return ApiKey.full(rawToken)
    }

    /**
     * @throws LoginException
     */
    private fun login(): ApiKey {
        if (!Config.hasUser()) {
            throw LoginException(LoginException.Reason.NO_USER)
        }
        val user = Config.user

        // try to login as guest
        try {
            val apiKey = loginGuest(user)
            if (Config.hasPassword()) {
                // upgrade account
                return upgrade(apiKey)
            } else {
                return apiKey
            }
        } catch (e: LoginException) {
            Log.d(TAG, "Guest login failed", e);
            when (e.reason) {
                LoginException.Reason.NEEDS_AUTH -> {
                }
                else -> throw e
            }
        }

        Log.d(TAG, "Trying full login")
        // try to login as full user
        try {
            return loginFull(user)
        } catch (e: LoginException) {
            Log.d(TAG, "Full login failed", e);
            throw e
        }
    }

    private fun loginGuest(user: String): ApiKey {
        val uuid = Settings.Secure.ANDROID_ID
        try {
            val rawToken = Connection.login(user, null, uuid)
            return ApiKey.guest(rawToken)
        } catch (e: ApiException) {
            when (e.code) {
                400 -> throw LoginException(LoginException.Reason.WRONG_UUID)
                401 -> throw LoginException(LoginException.Reason.NEEDS_AUTH)
                404 -> throw LoginException(LoginException.Reason.UNKNOWN)
                else -> throw UnknownAuthException("Error code: " + e.code, e)
            }
        }
    }

    private fun loginFull(user: String): ApiKey {
        if (!Config.hasPassword()) {
            throw LoginException(LoginException.Reason.NEEDS_AUTH)
        }
        val password = Config.password
        try {
            val rawToken = Connection.login(user, password, null)
            return ApiKey.full(rawToken)
        } catch (e: ApiException) {
            when (e.code) {
                401 -> throw LoginException(LoginException.Reason.NEEDS_AUTH)
                403 -> throw LoginException(LoginException.Reason.WRONG_PASSWORD)
                404 -> throw LoginException(LoginException.Reason.UNKNOWN)
                else -> throw UnknownAuthException(e)
            }
        }
    }
}

sealed class AuthException : Exception {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(cause: Throwable) : super(cause)
    constructor(message: String, cause: Throwable) : super(message, cause)

}

class RegisterException : AuthException {
    val reason: Reason

    constructor(reason: Reason) : super() {
        this.reason = reason
    }

    constructor(reason: Reason, message: String) : super(message) {
        this.reason = reason
    }

    enum class Reason {
        TAKEN, NO_USER
    }
}

class ChangePasswordException : AuthException {
    val reason: Reason

    constructor(reason: Reason) : super() {
        this.reason = reason
    }

    constructor(reason: Reason, message: String) : super(message) {
        this.reason = reason
    }

    enum class Reason {
        INVALID_PASSWORD, INVALID_TOKEN, WRONG_OLD_PASSWORD, NO_PASSWORD
    }
}

class LoginException : AuthException {
    val reason: Reason

    constructor(reason: Reason) : this(reason, "")
    constructor(reason: Reason, message: String) : super(message + " Reason: " + reason) {
        this.reason = reason
    }

    enum class Reason {
        WRONG_PASSWORD, WRONG_UUID, UNKNOWN, NEEDS_AUTH, NO_USER
    }
}

class ConnectionException : AuthException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}

class UnknownAuthException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: ApiException) : super(message, cause)
    constructor(cause: ApiException) : super(cause)
}
