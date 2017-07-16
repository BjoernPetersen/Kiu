package com.github.bjoernpetersen.q.api;

import android.content.Context;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.github.bjoernpetersen.jmusicbot.client.ApiClient;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import com.github.bjoernpetersen.jmusicbot.client.api.DefaultApi;
import com.github.bjoernpetersen.jmusicbot.client.model.PlayerState;
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.api.ApiKey.UserType;
import com.hadisatrio.optional.Optional;
import java.util.List;

@SuppressWarnings("unused")
public final class Connection {

  private static final String TAG = Connection.class.getSimpleName();

  @Nullable
  private static Connection instance;

  @NonNull
  private final Configuration configuration;
  @NonNull
  private final DefaultApi api;

  @Nullable
  private ApiKey apiKey;

  private Connection(@NonNull Context context) {
    this.configuration = new Configuration(PreferenceManager.getDefaultSharedPreferences(context));

    ApiClient apiClient = new ApiClient();
    apiClient.setBasePath(configuration.getBasePath());
    this.api = new DefaultApi(apiClient);
    this.apiKey = configuration.getApiKey().orNull();
  }

  @NonNull
  public synchronized static Connection get(@NonNull Context context) {
    if (instance == null) {
      instance = new Connection(context);
    }
    return instance;
  }

  @NonNull
  public static Connection get() {
    if (instance == null) {
      throw new IllegalStateException();
    }
    return instance;
  }

  @NonNull
  public Configuration getConfiguration() {
    return configuration;
  }

  public void reset() {
    this.apiKey = null;
    configuration.reset();
  }

  public void invalidateToken() {
    this.apiKey = null;
    getConfiguration().resetApiKey();
  }

  public boolean checkHasPermission(Permission permission) {
    try {
      if (hasPermission(getApiKey(), permission)) {
        return true;
      }
    } catch (ApiException e) {
      return false;
    }

    // Check if permission has changed on server side
    apiKey = null;
    try {
      return hasPermission(getApiKey(), permission);
    } catch (ApiException e) {
      return false;
    }
  }

  private boolean hasPermission(ApiKey apiKey, Permission permission) {
    return apiKey.getPermissions().contains(permission);
  }

  public void setHost(@NonNull String host) {
    getConfiguration().setHost(host);
    api.getApiClient().setBasePath(getConfiguration().getBasePath());
  }

  @NonNull
  private String getToken() throws ApiException {
    return getApiKey().getRaw();
  }

  @NonNull
  private synchronized ApiKey getApiKey() throws ApiException {
    ApiKey apiKey = this.apiKey;
    if (apiKey == null || apiKey.isExpired()) {
      UserType userType = apiKey == null ? null : apiKey.getUserType();
      getConfiguration().updateApiKey(apiKey = retrieveApiKey(userType));
    }
    return apiKey;
  }

  @NonNull
  private ApiKey retrieveApiKey(@Nullable UserType userType) throws ApiException {
    String userName = getConfiguration().getUserName().get();
    if (userType != null) {
      try {
        switch (userType) {
          case GUEST:
            return retrieveGuestApiKey(userName);
          case FULL:
            return retrieveFullApiKey(userName);
          default:
            throw new IllegalArgumentException();
        }
      } catch (ApiException e) {
        if (e.getCode() != 404) {
          throw e;
        }
      }
    }

    ApiKey guestKey;
    try {
      guestKey = register(userName);
    } catch (ApiException e) {
      if (e.getCode() == 409) {
        // Username is already in use, try to log in
        guestKey = tryLogin(userName);
        if (guestKey.getUserType() == UserType.FULL) {
          return guestKey;
        }
      } else {
        throw e;
      }
    }

    Optional<String> password = getConfiguration().getPassword();
    if (password.isPresent()) {
      return upgrade(guestKey, password.get());
    } else {
      return guestKey;
    }
  }

  private ApiKey tryLogin(String userName) throws ApiException {
    try {
      return retrieveGuestApiKey(userName);
    } catch (ApiException e) {
      if (e.getCode() == 401 && getConfiguration().getPassword().isPresent()) {
        // Needs password
        return retrieveFullApiKey(userName);
      } else {
        throw e;
      }
    }
  }

  private ApiKey retrieveGuestApiKey(String userName) throws ApiException {
    try {
      return ApiKey.guest(login(userName, null, Secure.ANDROID_ID));
    } catch (InvalidApiKeyException e) {
      throw new ApiException(e);
    }
  }

  private ApiKey retrieveFullApiKey(String userName) throws ApiException {
    Optional<String> password = getConfiguration().getPassword();
    try {
      return ApiKey.full(login(userName, password.get(), null));
    } catch (InvalidApiKeyException e) {
      throw new ApiException(e);
    }
  }

  private ApiKey register(String userName) throws ApiException {
    try {
      return ApiKey.guest(registerUser(userName, Secure.ANDROID_ID));
    } catch (InvalidApiKeyException e) {
      throw new ApiException(e);
    }
  }

  public boolean isFullUser() {
    return apiKey != null && apiKey.getUserType() == UserType.FULL;
  }

  public void setUsername(String username) throws ApiException {
    getConfiguration().setUserName(username);
    apiKey = null;
    getApiKey();
  }

  public void upgrade(@NonNull String password) throws ApiException {
    ApiKey apiKey = getApiKey();
    if (apiKey.getUserType() == UserType.FULL) {
      throw new IllegalStateException();
    }

    password = password.trim();
    Configuration config = getConfiguration();
    config.updateApiKey(this.apiKey = upgrade(apiKey, password));
    config.setPassword(password);
  }

  private ApiKey upgrade(ApiKey guestKey, String password) throws ApiException {
    try {
      return ApiKey.full(changePassword(guestKey.getRaw(), password, null));
    } catch (InvalidApiKeyException e) {
      throw new ApiException(e);
    }
  }

  public void changePassword(@NonNull String password) throws ApiException {
    ApiKey apiKey = getApiKey();
    Optional<String> oldPassword = getConfiguration().getPassword();
    if (apiKey.getUserType() == UserType.GUEST || !oldPassword.isPresent()) {
      throw new IllegalStateException();
    }

    try {
      String token = changePassword(apiKey.getRaw(), password, oldPassword.get());
      this.apiKey = apiKey = ApiKey.full(token);
    } catch (InvalidApiKeyException e) {
      throw new ApiException(e);
    }
    Configuration config = getConfiguration();
    config.updateApiKey(apiKey);
    config.setPassword(password);
  }

  /**
   * Sets a new password Sets a new password for the caller. If the user was a guest account, this
   * makes him a full user.
   *
   * @param authorization an authorization token
   * @param password A password (required)
   * @param oldPassword The users old password. Only required if the user is no guest (optional)
   * @return String
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  private String changePassword(String authorization, String password, String oldPassword)
      throws ApiException {
    return api.changePassword(authorization, password, oldPassword);
  }

  /**
   * Deletes a user
   * Deletes the user associated with the Authorization token.
   *
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  private void deleteUser() throws ApiException {
    try {
      api.deleteUser(getToken());
    } catch (ApiException e) {
      if (e.getCode() == 401) {
        Log.v(TAG, "Dropping invalid token");
        apiKey = null;
        api.deleteUser(getToken());
      } else {
        throw e;
      }
    }
  }

  /**
   * Removes a Song from the queue Removes the specified Song from the current queue. If the queue
   * did not contain the entry, nothing is done.
   *
   * @param queueEntry the queue entry to dequeue (required)
   * @return List&lt;QueueEntry&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public List<QueueEntry> dequeue(QueueEntry queueEntry) throws ApiException {
    try {
      return api.dequeue(getToken(), queueEntry);
    } catch (ApiException e) {
      if (e.getCode() == 401) {
        Log.v(TAG, "Dropping invalid token");
        apiKey = null;
        return api.dequeue(getToken(), queueEntry);
      } else {
        throw e;
      }
    }
  }

  /**
   * Adds a Song to the queue Adds the specified Song to the current queue. If the queue already
   * contains the Song, it won&#39;t be added.
   *
   * @param songId The entry&#39;s ID (required)
   * @param providerId The ID of the provider the entry is from (required)
   * @return List&lt;QueueEntry&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public List<QueueEntry> enqueue(String songId, String providerId) throws ApiException {
    try {
      return api.enqueue(getToken(), songId, providerId);
    } catch (ApiException e) {
      if (e.getCode() == 401) {
        Log.v(TAG, "Dropping invalid token");
        apiKey = null;
        return api.enqueue(getToken(), songId, providerId);
      } else {
        throw e;
      }
    }
  }

  /**
   * Returns the current player state Returns the current player state. If the state is PLAY or
   * PAUSE, it also contains the current entry.
   *
   * @return PlayerState
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public PlayerState getPlayerState() throws ApiException {
    return api.getPlayerState();
  }

  /**
   * Returns a list of all available providers
   *
   * @return List&lt;String&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public List<String> getProviders() throws ApiException {
    return api.getProviders();
  }

  /**
   * Returns the current player queue
   *
   * @return List&lt;QueueEntry&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public List<QueueEntry> getQueue() throws ApiException {
    return api.getQueue();
  }

  /**
   * Returns a list of all available suggesters
   *
   * @return List&lt;String&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public List<String> getSuggesters() throws ApiException {
    return api.getSuggesters();
  }

  /**
   * Retrieves a token for a user Retrieves an Authorization token for a user. Either a password or
   * a UUID must be supplied. Not both.
   *
   * @param userName The user to log in as (required)
   * @param password The users password. Guest users should use the uuid parameter. (optional)
   * @param uuid The UUID (or device ID) authenticating this guest user. Full users should use the
   * password parameter. (optional)
   * @return String
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  private String login(String userName, String password, String uuid) throws ApiException {
    return api.login(userName, password, uuid);
  }

  /**
   * Looks up a entry
   * Looks up a entry using its ID and a provider ID
   *
   * @param songId A entry ID (required)
   * @param providerId A provider ID (required)
   * @return Song
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public Song lookupSong(String songId,
      String providerId) throws ApiException {
    return api.lookupSong(songId, providerId);
  }

  /**
   * Skips to the next entry
   * Skips the current entry and plays the next entry.
   *
   * @return PlayerState
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public PlayerState nextSong() throws ApiException {
    try {
      return api.nextSong(getToken());
    } catch (ApiException e) {
      if (e.getCode() == 401) {
        Log.v(TAG, "Dropping invalid token");
        apiKey = null;
        return api.nextSong(getToken());
      } else {
        throw e;
      }
    }
  }

  /**
   * Pauses the player
   * Pauses the current playback. If the current player state is not PLAY, does nothing.
   *
   * @return PlayerState
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public PlayerState pausePlayer() throws ApiException {
    return api.pausePlayer();
  }

  /**
   * Registers a new user
   * Adds a new guest user to the database. The user is identified by his username.
   *
   * @param userName The desired user name (required)
   * @param uuid A uuid (or device ID) to authenticate the user while he doesn&#39;t have a password
   * (required)
   * @return String
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  private String registerUser(String userName, String uuid) throws ApiException {
    return api.registerUser(userName, uuid);
  }

  /**
   * Removes a entry from the suggestions
   *
   * @param suggesterId the ID of the suggester (required)
   * @param songId The ID of the entry to remove (required)
   * @param providerId The ID of the provider of the entry to remove (required)
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public void removeSuggestion(String suggesterId, String authorization, String songId,
      String providerId) throws ApiException {
    api.removeSuggestion(suggesterId, authorization, songId, providerId);
  }

  /**
   * Resumes the player
   * Pauses the current playback. If the current player state is not PAUSE, does nothing.
   *
   * @return PlayerState
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public PlayerState resumePlayer() throws ApiException {
    return api.resumePlayer();
  }

  /**
   * Searches for songs
   *
   * @param providerId The provider with which to search (required)
   * @param query A search query (required)
   * @return List&lt;Song&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public List<Song> searchSong(
      String providerId, String query) throws ApiException {
    return api.searchSong(providerId, query);
  }

  /**
   * Returns a list of suggestions
   *
   * @param suggesterId A suggester ID (required)
   * @param max The maximum size of the response. Defaults to 16. (optional)
   * @return List&lt;Song&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public List<Song> suggestSong(String suggesterId, Integer max) throws ApiException {
    return api.suggestSong(suggesterId, max);
  }
}
