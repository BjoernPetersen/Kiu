package com.github.bjoernpetersen.q.api

import com.github.bjoernpetersen.jmusicbot.client.ApiClient
import com.github.bjoernpetersen.jmusicbot.client.ApiException
import com.github.bjoernpetersen.jmusicbot.client.api.DefaultApi
import com.github.bjoernpetersen.jmusicbot.client.model.NamedPlugin
import com.github.bjoernpetersen.jmusicbot.client.model.PlayerState
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry
import com.github.bjoernpetersen.jmusicbot.client.model.Song

object Connection {
    private val api: DefaultApi
    var basePath: String
        get() = Config.basePath
        set(value) {
            api.apiClient.basePath = value
        }

    init {
        val apiClient = ApiClient()
        apiClient.basePath = basePath
        api = DefaultApi(apiClient)
    }

    /**
     * Sets a new password
     * Sets a new password for the caller. If the user was a guest account, this makes him a full user.
     * @param authorization An authorization token (required)
     * *
     * @param password A password (required)
     * *
     * @param oldPassword The users old password. Only required if the user is no guest (optional)
     * *
     * @return String
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun changePassword(authorization: String, password: String, oldPassword: String?): String {
        return api.changePassword(authorization, password, oldPassword)
    }

    /**
     * Deletes a user
     * Deletes the user associated with the Authorization token.
     * @param authorization An authorization token (required)
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun deleteUser(authorization: String) {
        api.deleteUser(authorization)
    }

    /**
     * Removes a Song from the queue
     * Removes the specified Song from the current queue. If the queue did not contain the song, nothing is done.
     * @param authorization Authorization token with &#39;skip&#39; permission (required)
     * @param songId the song ID of the song to dequeue (required)
     * @param providerId the provider ID of the song to dequeue (required)
     * @return List&lt;QueueEntry&gt;
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun dequeue(authorization: String, songId: String, providerId: String): List<QueueEntry> {
        return api.dequeue(authorization, songId, providerId)
    }

    /**
     * Adds a Song to the queue
     * Adds the specified Song to the current queue. If the queue already contains the Song, it won&#39;t be added.
     * @param authorization Authorization token (required)
     * *
     * @param songId The song&#39;s ID (required)
     * *
     * @param providerId The ID of the provider the song is from (required)
     * *
     * @return List&lt;QueueEntry&gt;
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun enqueue(
            authorization: String, songId: String, providerId: String): List<QueueEntry> {
        return api.enqueue(authorization, songId, providerId)
    }

    /**
     * Returns the current player state
     * Returns the current player state. If the state is PLAY or PAUSE, it also contains the current song.
     * @return PlayerState
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun getPlayerState(): PlayerState {
        return api.playerState
    }

    /**
     * Returns a list of all available providers

     * @return List&lt;String&gt;
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun getProviders(): List<NamedPlugin> {
        return api.providers
    }

    /**
     * Returns the current player queue

     * @return List&lt;QueueEntry&gt;
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun getQueue(): List<QueueEntry> {
        return api.queue
    }

    /**
     * Returns a list of all available suggesters

     * @return List&lt;String&gt;
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun getSuggesters(): List<NamedPlugin> {
        return api.suggesters
    }

    /**
     * Retrieves a token for a user
     * Retrieves an Authorization token for a user. Either a password or a UUID must be supplied. Not both.
     * @param userName The user to log in as (required)
     * *
     * @param password The users password. Guest users should use the uuid parameter. (optional)
     * *
     * @param uuid The UUID (or device ID) authenticating this guest user. Full users should use the password parameter. (optional)
     * *
     * @return String
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun login(userName: String, password: String?, uuid: String?): String {
        return api.login(userName, password, uuid)
    }

    /**
     * Looks up a song
     * Looks up a song using its ID and a provider ID
     * @param songId A song ID (required)
     * *
     * @param providerId A provider ID (required)
     * *
     * @return Song
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun lookupSong(songId: String,
                   providerId: String): Song {
        return api.lookupSong(songId, providerId)
    }

    /**
     * Skips to the next song
     * Skips the current song and plays the next song.
     * @param authorization Authorization token with &#39;skip&#39; permission (required)
     * *
     * @return PlayerState
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun nextSong(
            authorization: String): PlayerState {
        return api.nextSong(authorization)
    }

    /**
     * Pauses the player
     * Pauses the current playback. If the current player state is not PLAY, does nothing.
     * @return PlayerState
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun pausePlayer(): PlayerState {
        return api.pausePlayer()
    }

    /**
     * Registers a new user
     * Adds a new guest user to the database. The user is identified by his username.
     * @param userName The desired user name (required)
     * *
     * @param uuid A uuid (or device ID) to authenticate the user while he doesn&#39;t have a password (required)
     * *
     * @return String
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun registerUser(userName: String, uuid: String): String {
        return api.registerUser(userName, uuid)
    }

    /**
     * Removes a song from the suggestions

     * @param suggesterId the ID of the suggester (required)
     * *
     * @param authorization An authorization token with &#39;dislike&#39; permission (required)
     * *
     * @param songId The ID of the song to remove (required)
     * *
     * @param providerId The ID of the provider of the song to remove (required)
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun removeSuggestion(suggesterId: String, authorization: String, songId: String,
                         providerId: String) {
        api.removeSuggestion(suggesterId, authorization, songId, providerId)
    }

    /**
     * Resumes the player
     * Pauses the current playback. If the current player state is not PAUSE, does nothing.
     * @return PlayerState
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun resumePlayer(): PlayerState {
        return api.resumePlayer()
    }

    /**
     * Searches for songs

     * @param providerId The provider with which to search (required)
     * *
     * @param query A search query (required)
     * *
     * @return List&lt;Song&gt;
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun searchSong(
            providerId: String, query: String): List<Song> {
        return api.searchSong(providerId, query)
    }

    /**
     * Returns a list of suggestions

     * @param suggesterId A suggester ID (required)
     * *
     * @param max The maximum size of the response. Defaults to 16. (optional)
     * *
     * @return List&lt;Song&gt;
     * *
     * @throws ApiException If fail to call the API, e.g. server error or cannot deserialize the response body
     */
    @Throws(ApiException::class)
    fun suggestSong(
            suggesterId: String, max: Int?): List<Song> {
        return api.suggestSong(suggesterId, max)
    }

}