package com.github.bjoernpetersen.q.api;

import android.support.annotation.NonNull;
import com.auth0.android.jwt.DecodeException;
import com.auth0.android.jwt.JWT;
import java.security.PrivilegedActionException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

final class ApiKey {

  enum UserType {
    GUEST, FULL
  }

  @NonNull
  private final String raw;
  @NonNull
  private final UserType userType;

  @NonNull
  private final String user;
  @NonNull
  private final Set<Permission> permissions;
  @NonNull
  private final Date expiresAt;

  ApiKey(@NonNull String raw, @NonNull UserType userType)
      throws InvalidApiKeyException {
    this.raw = raw;
    this.userType = userType;

    JWT jwt;
    try {
      jwt = new JWT(raw);
    } catch (DecodeException e) {
      throw new InvalidApiKeyException(e);
    }

    String subject = jwt.getSubject();
    if (subject == null) {
      throw new InvalidApiKeyException("Subject is null");
    }
    this.user = subject;

    Date expiresAt = jwt.getExpiresAt();
    if (expiresAt == null) {
      throw new InvalidApiKeyException("Expires at is null");
    }
    this.expiresAt = expiresAt;

    Set<Permission> permissions = new HashSet<>();
    for (Permission permission : Permission.values()) {
      String claim = jwt.getClaim(permission.getName()).asString();
      if (claim != null) {
        permissions.add(permission);
      }
    }
    this.permissions = Collections.unmodifiableSet(permissions);
  }

  static ApiKey guest(@NonNull String raw) throws InvalidApiKeyException {
    return new ApiKey(raw, UserType.GUEST);
  }

  static ApiKey full(@NonNull String raw) throws InvalidApiKeyException {
    return new ApiKey(raw, UserType.FULL);
  }

  @NonNull
  String getRaw() {
    if (isExpired()) {
      throw new IllegalStateException();
    }
    return raw;
  }

  @NonNull
  UserType getUserType() {
    return userType;
  }

  @NonNull
  public String getUser() {
    return user;
  }

  @NonNull
  public boolean isExpired() {
    return new Date().after(expiresAt);
  }

  @NonNull
  public Set<Permission> getPermissions() {
    return permissions;
  }
}

final class InvalidApiKeyException extends Exception {

  /**
   * Constructs a new exception with {@code null} as its detail message.
   * The cause is not initialized, and may subsequently be initialized by a
   * call to {@link #initCause}.
   */
  public InvalidApiKeyException() {
  }

  /**
   * Constructs a new exception with the specified detail message.  The
   * cause is not initialized, and may subsequently be initialized by
   * a call to {@link #initCause}.
   *
   * @param message the detail message. The detail message is saved for later retrieval by the
   * {@link #getMessage()} method.
   */
  public InvalidApiKeyException(String message) {
    super(message);
  }

  /**
   * Constructs a new exception with the specified detail message and
   * cause.  <p>Note that the detail message associated with
   * {@code cause} is <i>not</i> automatically incorporated in
   * this exception's detail message.
   *
   * @param message the detail message (which is saved for later retrieval by the {@link
   * #getMessage()} method).
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   * (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
   * @since 1.4
   */
  public InvalidApiKeyException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Constructs a new exception with the specified cause and a detail
   * message of <tt>(cause==null ? null : cause.toString())</tt> (which
   * typically contains the class and detail message of <tt>cause</tt>).
   * This constructor is useful for exceptions that are little more than
   * wrappers for other throwables (for example, {@link
   * PrivilegedActionException}).
   *
   * @param cause the cause (which is saved for later retrieval by the {@link #getCause()} method).
   * (A <tt>null</tt> value is permitted, and indicates that the cause is nonexistent or unknown.)
   * @since 1.4
   */
  public InvalidApiKeyException(Throwable cause) {
    super(cause);
  }
}
