package com.github.bjoernpetersen.q.star

import android.arch.persistence.room.*
import android.content.Context
import com.github.bjoernpetersen.jmusicbot.client.model.NamedPlugin
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class StarredAccess private constructor(context: Context) : AutoCloseable {
  private val db = Room.databaseBuilder(context, StarredDatabase::class.java, DB_NAME).build()

  fun getAll(): Flowable<List<Song>> = db.starredDao().getAll()
      .subscribeOn(Schedulers.io())
      .map { it.map { it.toSong() } }
      .observeOn(AndroidSchedulers.mainThread())

  fun add(song: Song) = Completable.fromAction { db.starredDao().insert(song.toDbSong()) }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())

  fun remove(song: Song) = Completable.fromAction { db.starredDao().delete(song.toDbSong()) }
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())

  fun clear() {
    TODO("Not implemented yet")
  }

  override fun close() = db.close()

  companion object {
    @JvmStatic
    private val DB_NAME = "StarredSongs"
    private var instance: StarredAccess? = null

    fun instance(context: Context): StarredAccess {
      if (instance == null) {
        instance = StarredAccess(context)
      }
      return instance!!
    }
  }
}

private fun DbSong.toSong(): Song = Song()
    .id(id)
    .provider(NamedPlugin().id(providerId).name(providerId))
    .title(title)
    .description(description)
    .albumArtUrl(albumArtUrl)
    .duration(duration)

private fun Song.toDbSong(): DbSong = DbSong(
    id,
    provider.id,
    title,
    description,
    duration,
    albumArtUrl
)

@Entity(primaryKeys = arrayOf("id", "providerId"), tableName = "songs")
private class DbSong(
    var id: String = "", var providerId: String = "",
    var title: String = "",
    var description: String = "",
    var duration: Int = 0,
    var albumArtUrl: String? = null) {

  override fun toString(): String {
    return "DbSong(id='$id', providerId='$providerId', title='$title', description='$description', duration=$duration, albumArtUrl=$albumArtUrl)"
  }
}

@Dao
private interface StarredDao {

  @Query("SELECT * FROM songs")
  fun getAll(): Flowable<List<DbSong>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insert(song: DbSong)

  @Delete
  fun delete(song: DbSong)

  @Query("SELECT * FROM songs WHERE id = :id AND providerId = :providerId")
  fun getSongById(id: String, providerId: String): Single<DbSong>
}

@Database(entities = arrayOf(DbSong::class), version = 1)
private abstract class StarredDatabase : RoomDatabase() {

  abstract fun starredDao(): StarredDao
}
