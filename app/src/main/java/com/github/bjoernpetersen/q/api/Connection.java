package com.github.bjoernpetersen.q.api;

import android.content.Context;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.github.bjoernpetersen.jmusicbot.client.ApiCallback;
import com.github.bjoernpetersen.jmusicbot.client.ApiClient;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import com.github.bjoernpetersen.jmusicbot.client.ApiResponse;
import com.github.bjoernpetersen.jmusicbot.client.ProgressRequestBody.ProgressRequestListener;
import com.github.bjoernpetersen.jmusicbot.client.ProgressResponseBody.ProgressListener;
import com.github.bjoernpetersen.jmusicbot.client.api.DefaultApi;
import com.github.bjoernpetersen.jmusicbot.client.model.PlayerState;
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.api.ApiKey.UserType;
import com.hadisatrio.optional.Optional;
import com.squareup.okhttp.Call;
import java.util.List;

@SuppressWarnings("unused")
public final class Connection {


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
      switch (userType) {
        case GUEST:
          return retrieveGuestApiKey(userName);
        case FULL:
          return retrieveFullApiKey(userName);
        default:
          throw new IllegalArgumentException();
      }
    }

    ApiKey guestKey = register(userName);

    Optional<String> password = getConfiguration().getPassword();
    if (password.isPresent()) {
      return upgrade(guestKey, password.get());
    } else {
      return guestKey;
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

  public void upgrade(@NonNull String password) throws ApiException {
    ApiKey apiKey = getApiKey();
    if (apiKey.getUserType() == UserType.FULL) {
      throw new IllegalStateException();
    }

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
   * Build call for changePassword
   *
   * @param authorization an authorization token
   * @param password A password (required)
   * @param oldPassword The users old password. Only required if the user is no guest (optional)
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  private Call changePasswordCall(String authorization, String password, String oldPassword,
      ProgressListener progressListener, ProgressRequestListener progressRequestListener)
      throws ApiException {
    return api.changePasswordCall(authorization, password, oldPassword, progressListener,
        progressRequestListener);
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
   * Sets a new password Sets a new password for the caller. If the user was a guest account, this
   * makes him a full user.
   *
   * @param authorization an authorization token
   * @param password A password (required)
   * @param oldPassword The users old password. Only required if the user is no guest (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  private ApiResponse<String> changePasswordWithHttpInfo(String authorization, String password,
      String oldPassword) throws ApiException {
    return api.changePasswordWithHttpInfo(authorization, password, oldPassword);
  }

  /**
   * Sets a new password (asynchronously) Sets a new password for the caller. If the user was a
   * guest account, this makes him a full user.
   *
   * @param authorization an authorization token
   * @param password A password (required)
   * @param oldPassword The users old password. Only required if the user is no guest (optional)
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  private Call changePasswordAsync(String authorization, String password,
      String oldPassword, ApiCallback<String> callback) throws ApiException {
    return api.changePasswordAsync(authorization, password, oldPassword, callback);
  }

  /**
   * Build call for deleteUser
   *
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  private Call deleteUserCall(String authorization,
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.deleteUserCall(getToken(), progressListener, progressRequestListener);
  }

  /**
   * Deletes a user
   * Deletes the user associated with the Authorization token.
   *
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  private void deleteUser(String authorization) throws ApiException {
    api.deleteUser(getToken());
  }

  /**
   * Deletes a user
   * Deletes the user associated with the Authorization token.
   *
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  private ApiResponse<Void> deleteUserWithHttpInfo(
      String authorization) throws ApiException {
    return api.deleteUserWithHttpInfo(getToken());
  }

  /**
   * Deletes a user (asynchronously)
   * Deletes the user associated with the Authorization token.
   *
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  private Call deleteUserAsync(String authorization,
      ApiCallback<Void> callback) throws ApiException {
    return api.deleteUserAsync(getToken(), callback);
  }

  /**
   * Build call for dequeue
   *
   * @param queueEntry the queue entry to dequeue (required)
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call dequeueCall(String authorization,
      QueueEntry queueEntry,
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.dequeueCall(getToken(), queueEntry, progressListener, progressRequestListener);
  }

  /**
   * Removes a Song from the queue Removes the specified Song from the current queue. If the queue
   * did not contain the song, nothing is done.
   *
   * @param queueEntry the queue entry to dequeue (required)
   * @return List&lt;QueueEntry&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public List<QueueEntry> dequeue(
      String authorization, QueueEntry queueEntry) throws ApiException {
    return api.dequeue(getToken(), queueEntry);
  }

  /**
   * Removes a Song from the queue Removes the specified Song from the current queue. If the queue
   * did not contain the song, nothing is done.
   *
   * @param queueEntry the queue entry to dequeue (required)
   * @return ApiResponse&lt;List&lt;QueueEntry&gt;&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<List<QueueEntry>> dequeueWithHttpInfo(
      String authorization, QueueEntry queueEntry) throws ApiException {
    return api.dequeueWithHttpInfo(getToken(), queueEntry);
  }

  /**
   * Removes a Song from the queue (asynchronously) Removes the specified Song from the current
   * queue. If the queue did not contain the song, nothing is done.
   *
   * @param queueEntry the queue entry to dequeue (required)
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call dequeueAsync(String authorization,
      QueueEntry queueEntry,
      ApiCallback<List<QueueEntry>> callback) throws ApiException {
    return api.dequeueAsync(getToken(), queueEntry, callback);
  }

  /**
   * Build call for enqueue
   *
   * @param songId The song&#39;s ID (required)
   * @param providerId The ID of the provider the song is from (required)
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call enqueueCall(String songId,
      String providerId,
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api
        .enqueueCall(getToken(), songId, providerId, progressListener, progressRequestListener);
  }

  /**
   * Adds a Song to the queue Adds the specified Song to the current queue. If the queue already
   * contains the Song, it won&#39;t be added.
   *
   * @param songId The song&#39;s ID (required)
   * @param providerId The ID of the provider the song is from (required)
   * @return List&lt;QueueEntry&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public List<QueueEntry> enqueue(
      String authorization, String songId, String providerId) throws ApiException {
    return api.enqueue(getToken(), songId, providerId);
  }

  /**
   * Adds a Song to the queue Adds the specified Song to the current queue. If the queue already
   * contains the Song, it won&#39;t be added.
   *
   * @param songId The song&#39;s ID (required)
   * @param providerId The ID of the provider the song is from (required)
   * @return ApiResponse&lt;List&lt;QueueEntry&gt;&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<List<QueueEntry>> enqueueWithHttpInfo(
      String authorization, String songId, String providerId) throws ApiException {
    return api.enqueueWithHttpInfo(getToken(), songId, providerId);
  }

  /**
   * Adds a Song to the queue (asynchronously) Adds the specified Song to the current queue. If the
   * queue already contains the Song, it won&#39;t be added.
   *
   * @param songId The song&#39;s ID (required)
   * @param providerId The ID of the provider the song is from (required)
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call enqueueAsync(String songId,
      String providerId,
      ApiCallback<List<QueueEntry>> callback) throws ApiException {
    return api.enqueueAsync(getToken(), songId, providerId, callback);
  }

  /**
   * Build call for getPlayerState
   *
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call getPlayerStateCall(
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.getPlayerStateCall(progressListener, progressRequestListener);
  }

  /**
   * Returns the current player state Returns the current player state. If the state is PLAY or
   * PAUSE, it also contains the current song.
   *
   * @return PlayerState
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public PlayerState getPlayerState() throws ApiException {
    return api.getPlayerState();
  }

  /**
   * Returns the current player state Returns the current player state. If the state is PLAY or
   * PAUSE, it also contains the current song.
   *
   * @return ApiResponse&lt;PlayerState&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<PlayerState> getPlayerStateWithHttpInfo() throws ApiException {
    return api.getPlayerStateWithHttpInfo();
  }

  /**
   * Returns the current player state (asynchronously) Returns the current player state. If the
   * state is PLAY or PAUSE, it also contains the current song.
   *
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call getPlayerStateAsync(
      ApiCallback<PlayerState> callback) throws ApiException {
    return api.getPlayerStateAsync(callback);
  }

  /**
   * Build call for getProviders
   *
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call getProvidersCall(
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.getProvidersCall(progressListener, progressRequestListener);
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
   * Returns a list of all available providers
   *
   * @return ApiResponse&lt;List&lt;String&gt;&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<List<String>> getProvidersWithHttpInfo() throws ApiException {
    return api.getProvidersWithHttpInfo();
  }

  /**
   * Returns a list of all available providers (asynchronously)
   *
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call getProvidersAsync(
      ApiCallback<List<String>> callback) throws ApiException {
    return api.getProvidersAsync(callback);
  }

  /**
   * Build call for getQueue
   *
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call getQueueCall(
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.getQueueCall(progressListener, progressRequestListener);
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
   * Returns the current player queue
   *
   * @return ApiResponse&lt;List&lt;QueueEntry&gt;&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<List<QueueEntry>> getQueueWithHttpInfo() throws ApiException {
    return api.getQueueWithHttpInfo();
  }

  /**
   * Returns the current player queue (asynchronously)
   *
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call getQueueAsync(
      ApiCallback<List<QueueEntry>> callback) throws ApiException {
    return api.getQueueAsync(callback);
  }

  /**
   * Build call for getSuggesters
   *
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call getSuggestersCall(
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.getSuggestersCall(progressListener, progressRequestListener);
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
   * Returns a list of all available suggesters
   *
   * @return ApiResponse&lt;List&lt;String&gt;&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<List<String>> getSuggestersWithHttpInfo() throws ApiException {
    return api.getSuggestersWithHttpInfo();
  }

  /**
   * Returns a list of all available suggesters (asynchronously)
   *
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call getSuggestersAsync(
      ApiCallback<List<String>> callback) throws ApiException {
    return api.getSuggestersAsync(callback);
  }

  /**
   * Build call for login
   *
   * @param userName The user to log in as (required)
   * @param password The users password. Guest users should use the uuid parameter. (optional)
   * @param uuid The UUID (or device ID) authenticating this guest user. Full users should use the
   * password parameter. (optional)
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  private Call loginCall(String userName, String password, String uuid,
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.loginCall(userName, password, uuid, progressListener, progressRequestListener);
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
   * Retrieves a token for a user Retrieves an Authorization token for a user. Either a password or
   * a UUID must be supplied. Not both.
   *
   * @param userName The user to log in as (required)
   * @param password The users password. Guest users should use the uuid parameter. (optional)
   * @param uuid The UUID (or device ID) authenticating this guest user. Full users should use the
   * password parameter. (optional)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  private ApiResponse<String> loginWithHttpInfo(
      String userName, String password, String uuid) throws ApiException {
    return api.loginWithHttpInfo(userName, password, uuid);
  }

  /**
   * Retrieves a token for a user (asynchronously) Retrieves an Authorization token for a user.
   * Either a password or a UUID must be supplied. Not both.
   *
   * @param userName The user to log in as (required)
   * @param password The users password. Guest users should use the uuid parameter. (optional)
   * @param uuid The UUID (or device ID) authenticating this guest user. Full users should use the
   * password parameter. (optional)
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  private Call loginAsync(String userName, String password, String uuid,
      ApiCallback<String> callback) throws ApiException {
    return api.loginAsync(userName, password, uuid, callback);
  }

  /**
   * Build call for lookupSong
   *
   * @param songId A song ID (required)
   * @param providerId A provider ID (required)
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call lookupSongCall(String songId, String providerId,
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.lookupSongCall(songId, providerId, progressListener, progressRequestListener);
  }

  /**
   * Looks up a song
   * Looks up a song using its ID and a provider ID
   *
   * @param songId A song ID (required)
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
   * Looks up a song
   * Looks up a song using its ID and a provider ID
   *
   * @param songId A song ID (required)
   * @param providerId A provider ID (required)
   * @return ApiResponse&lt;Song&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<Song> lookupSongWithHttpInfo(
      String songId, String providerId) throws ApiException {
    return api.lookupSongWithHttpInfo(songId, providerId);
  }

  /**
   * Looks up a song (asynchronously)
   * Looks up a song using its ID and a provider ID
   *
   * @param songId A song ID (required)
   * @param providerId A provider ID (required)
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call lookupSongAsync(String songId, String providerId,
      ApiCallback<Song> callback) throws ApiException {
    return api.lookupSongAsync(songId, providerId, callback);
  }

  /**
   * Build call for nextSong
   *
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call nextSongCall(String authorization,
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.nextSongCall(getToken(), progressListener, progressRequestListener);
  }

  /**
   * Skips to the next song
   * Skips the current song and plays the next song.
   *
   * @return PlayerState
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public PlayerState nextSong(
      String authorization) throws ApiException {
    return api.nextSong(getToken());
  }

  /**
   * Skips to the next song
   * Skips the current song and plays the next song.
   *
   * @return ApiResponse&lt;PlayerState&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<PlayerState> nextSongWithHttpInfo(
      String authorization) throws ApiException {
    return api.nextSongWithHttpInfo(getToken());
  }

  /**
   * Skips to the next song (asynchronously)
   * Skips the current song and plays the next song.
   *
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call nextSongAsync(String authorization,
      ApiCallback<PlayerState> callback) throws ApiException {
    return api.nextSongAsync(getToken(), callback);
  }

  /**
   * Build call for pausePlayer
   *
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call pausePlayerCall(
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.pausePlayerCall(progressListener, progressRequestListener);
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
   * Pauses the player
   * Pauses the current playback. If the current player state is not PLAY, does nothing.
   *
   * @return ApiResponse&lt;PlayerState&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<PlayerState> pausePlayerWithHttpInfo() throws ApiException {
    return api.pausePlayerWithHttpInfo();
  }

  /**
   * Pauses the player (asynchronously)
   * Pauses the current playback. If the current player state is not PLAY, does nothing.
   *
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call pausePlayerAsync(
      ApiCallback<PlayerState> callback) throws ApiException {
    return api.pausePlayerAsync(callback);
  }

  /**
   * Build call for registerUser
   *
   * @param userName The desired user name (required)
   * @param uuid A uuid (or device ID) to authenticate the user while he doesn&#39;t have a password
   * (required)
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  private Call registerUserCall(String userName, String uuid,
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.registerUserCall(userName, uuid, progressListener, progressRequestListener);
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
   * Registers a new user
   * Adds a new guest user to the database. The user is identified by his username.
   *
   * @param userName The desired user name (required)
   * @param uuid A uuid (or device ID) to authenticate the user while he doesn&#39;t have a password
   * (required)
   * @return ApiResponse&lt;String&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  private ApiResponse<String> registerUserWithHttpInfo(
      String userName, String uuid) throws ApiException {
    return api.registerUserWithHttpInfo(userName, uuid);
  }

  /**
   * Registers a new user (asynchronously)
   * Adds a new guest user to the database. The user is identified by his username.
   *
   * @param userName The desired user name (required)
   * @param uuid A uuid (or device ID) to authenticate the user while he doesn&#39;t have a password
   * (required)
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  private Call registerUserAsync(String userName, String uuid,
      ApiCallback<String> callback) throws ApiException {
    return api.registerUserAsync(userName, uuid, callback);
  }

  /**
   * Build call for removeSuggestion
   *
   * @param suggesterId the ID of the suggester (required)
   * @param songId The ID of the song to remove (required)
   * @param providerId The ID of the provider of the song to remove (required)
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call removeSuggestionCall(String suggesterId,
      String authorization, String songId, String providerId,
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api
        .removeSuggestionCall(suggesterId, authorization, songId, providerId, progressListener,
            progressRequestListener);
  }

  /**
   * Removes a song from the suggestions
   *
   * @param suggesterId the ID of the suggester (required)
   * @param songId The ID of the song to remove (required)
   * @param providerId The ID of the provider of the song to remove (required)
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public void removeSuggestion(String suggesterId, String authorization, String songId,
      String providerId) throws ApiException {
    api.removeSuggestion(suggesterId, authorization, songId, providerId);
  }

  /**
   * Removes a song from the suggestions
   *
   * @param suggesterId the ID of the suggester (required)
   * @param songId The ID of the song to remove (required)
   * @param providerId The ID of the provider of the song to remove (required)
   * @return ApiResponse&lt;Void&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<Void> removeSuggestionWithHttpInfo(
      String suggesterId, String authorization, String songId, String providerId)
      throws ApiException {
    return api.removeSuggestionWithHttpInfo(suggesterId, authorization, songId, providerId);
  }

  /**
   * Removes a song from the suggestions (asynchronously)
   *
   * @param suggesterId the ID of the suggester (required)
   * @param songId The ID of the song to remove (required)
   * @param providerId The ID of the provider of the song to remove (required)
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call removeSuggestionAsync(String suggesterId,
      String authorization, String songId, String providerId,
      ApiCallback<Void> callback) throws ApiException {
    return api.removeSuggestionAsync(suggesterId, authorization, songId, providerId, callback);
  }

  /**
   * Build call for resumePlayer
   *
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call resumePlayerCall(
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.resumePlayerCall(progressListener, progressRequestListener);
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
   * Resumes the player
   * Pauses the current playback. If the current player state is not PAUSE, does nothing.
   *
   * @return ApiResponse&lt;PlayerState&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<PlayerState> resumePlayerWithHttpInfo() throws ApiException {
    return api.resumePlayerWithHttpInfo();
  }

  /**
   * Resumes the player (asynchronously)
   * Pauses the current playback. If the current player state is not PAUSE, does nothing.
   *
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call resumePlayerAsync(
      ApiCallback<PlayerState> callback) throws ApiException {
    return api.resumePlayerAsync(callback);
  }

  /**
   * Build call for searchSong
   *
   * @param providerId The provider with which to search (required)
   * @param query A search query (required)
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call searchSongCall(String providerId, String query,
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.searchSongCall(providerId, query, progressListener, progressRequestListener);
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
   * Searches for songs
   *
   * @param providerId The provider with which to search (required)
   * @param query A search query (required)
   * @return ApiResponse&lt;List&lt;Song&gt;&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<List<Song>> searchSongWithHttpInfo(
      String providerId, String query) throws ApiException {
    return api.searchSongWithHttpInfo(providerId, query);
  }

  /**
   * Searches for songs (asynchronously)
   *
   * @param providerId The provider with which to search (required)
   * @param query A search query (required)
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call searchSongAsync(String providerId, String query,
      ApiCallback<List<Song>> callback) throws ApiException {
    return api.searchSongAsync(providerId, query, callback);
  }

  /**
   * Build call for suggestSong
   *
   * @param suggesterId A suggester ID (required)
   * @param max The maximum size of the response. Defaults to 16. (optional)
   * @param progressListener Progress listener
   * @param progressRequestListener Progress request listener
   * @return Call to execute
   * @throws ApiException If fail to serialize the request body object
   */
  public Call suggestSongCall(String suggesterId, Integer max,
      ProgressListener progressListener,
      ProgressRequestListener progressRequestListener) throws ApiException {
    return api.suggestSongCall(suggesterId, max, progressListener, progressRequestListener);
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
  public List<Song> suggestSong(
      String suggesterId, Integer max) throws ApiException {
    return api.suggestSong(suggesterId, max);
  }

  /**
   * Returns a list of suggestions
   *
   * @param suggesterId A suggester ID (required)
   * @param max The maximum size of the response. Defaults to 16. (optional)
   * @return ApiResponse&lt;List&lt;Song&gt;&gt;
   * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the
   * response body
   */
  public ApiResponse<List<Song>> suggestSongWithHttpInfo(
      String suggesterId, Integer max) throws ApiException {
    return api.suggestSongWithHttpInfo(suggesterId, max);
  }

  /**
   * Returns a list of suggestions (asynchronously)
   *
   * @param suggesterId A suggester ID (required)
   * @param max The maximum size of the response. Defaults to 16. (optional)
   * @param callback The callback to be executed when the API call finishes
   * @return The request call
   * @throws ApiException If fail to process the API call, e.g. serializing the request body object
   */
  public Call suggestSongAsync(String suggesterId, Integer max,
      ApiCallback<List<Song>> callback) throws ApiException {
    return api.suggestSongAsync(suggesterId, max, callback);
  }
}
