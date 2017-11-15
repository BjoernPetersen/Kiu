package com.github.bjoernpetersen.q.api

import com.auth0.android.jwt.DecodeException
import com.auth0.android.jwt.JWT
import java.util.Date
import kotlin.collections.HashSet

class ApiKey @Throws(InvalidApiKeyException::class)
constructor(private val _raw: String, val userType: UserType) {

  enum class UserType {
    GUEST, FULL
  }

  val userName: String
  val permissions: Set<Permission>
  private val expiresAt: Date

  init {
    val jwt = try {
      JWT(_raw)
    } catch (e: DecodeException) {
      throw InvalidApiKeyException(e)
    }

    val subject = jwt.subject ?: throw InvalidApiKeyException("Subject is null")
    this.userName = subject

    val expiresAt = jwt.expiresAt ?: throw InvalidApiKeyException("Expires at is null")
    this.expiresAt = expiresAt

    val permissions = HashSet<Permission>()
    for (permission in Permission.values()) {
      val claim = jwt.getClaim(permission.label).asString()
      if (claim != null) {
        permissions.add(permission)
      }
    }
    this.permissions = permissions.toSet()
  }

  val isExpired: Boolean
    get() = Date().after(expiresAt)

  val raw: String
    get() {
      if (isExpired) {
        throw IllegalStateException()
      }
      return _raw
    }

  companion object {
    @Throws(InvalidApiKeyException::class)
    @JvmStatic
    fun guest(raw: String): ApiKey {
      return ApiKey(raw, UserType.GUEST)
    }

    @Throws(InvalidApiKeyException::class)
    @JvmStatic
    fun full(raw: String): ApiKey {
      return ApiKey(raw, UserType.FULL)
    }
  }
}

class InvalidApiKeyException : Exception {
  constructor() : super()
  constructor(message: String) : super(message)
  constructor(message: String, cause: Throwable) : super(message, cause)
  constructor(cause: Throwable) : super(cause)
}
