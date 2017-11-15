package com.github.bjoernpetersen.q.ui.fragments


import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.GestureDetector.OnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.Toast
import com.github.bjoernpetersen.jmusicbot.client.model.PlayerState
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.Auth
import com.github.bjoernpetersen.q.api.Connection
import com.github.bjoernpetersen.q.api.Permission
import com.github.bjoernpetersen.q.tag
import com.github.bjoernpetersen.q.ui.ObserverUser
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_player.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

private const val SWIPE_THRESHOLD = 100
private const val SWIPE_VELOCITY_THRESHOLD = 100

class PlayerFragment : Fragment(), ObserverUser {

  override lateinit var observers: MutableList<WeakReference<Disposable>>

  override fun initObservers() {
    observers = ArrayList()
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    // Inflate the layout for this fragment
    return inflater!!.inflate(R.layout.fragment_player, container, false)
  }

  override fun onActivityCreated(savedInstanceState: Bundle?) {
    super.onActivityCreated(savedInstanceState)
    val view = view ?: throw IllegalStateException()
    song_title.isSelected = true
    song_description.isSelected = true
    song_queuer.isSelected = true
    view.setOnTouchListener(NextSwipeListener(context))
  }

  override fun onStart() {
    super.onStart()
    initObservers()
  }

  override fun onStop() {
    disposeObservers()
    super.onStop()
  }

  private fun invokeStateUpdate() {
    val mainLooper = Looper.getMainLooper()
    val scheduler = Schedulers.newThread()
    scheduler.schedulePeriodicallyDirect({
      Observable.fromCallable { Connection.getPlayerState() }
          .subscribeOn(scheduler)
          .observeOn(AndroidSchedulers.from(mainLooper))
          .subscribe(
              { if (isVisible) updatedState(it) else scheduler.shutdown() },
              { Log.v(tag(), "Error updating player fragment", it) }
          )
    }, 0, 2, TimeUnit.SECONDS)
  }

  override fun onResume() {
    super.onResume()
    invokeStateUpdate()
  }

  private fun pause() {
    Observable.fromCallable { Connection.pausePlayer() }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe({ updatedState(it) }, { Log.d(tag(), "Could not pause player", it) })
        .store()
  }

  private fun play() {
    Observable.fromCallable { Connection.resumePlayer() }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeOn(Schedulers.io())
        .subscribe({ updatedState(it) }, { Log.d(tag(), "Could not resume player", it) })
        .store()
  }

  private operator fun next() {
    Single.fromCallable { Auth.hasPermission(Permission.SKIP) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .retry(2)
        .subscribe({
          if (it && isVisible) {
            val toast: Toast = Toast
                .makeText(context, R.string.skipping_current_song, Toast.LENGTH_SHORT)
                .apply { show() }
            Observable.fromCallable { Auth.apiKey.raw }
                .subscribeOn(Schedulers.io())
                .map { Connection.nextSong(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { updatedState(it) },
                    {
                      toast.cancel()
                      Log.i(tag(), "Could not skip current song", it)
                      Toast.makeText(context, R.string.skip_error, Toast.LENGTH_SHORT).show()
                      Auth.clear()
                    }
                )
                .store()
          }
        }, { Log.d(tag(), "Could not retrieve permissions") })
        .store()
  }

  private fun updatedState(playerState: PlayerState) {
    // update play/pause button
    val state = playerState.state
    val playPause = play_pause
    if (state == PlayerState.StateEnum.PLAY) {
      // show pause button
      playPause.setImageResource(R.drawable.ic_pause_circle_filled)
      playPause.setOnClickListener { pause() }
    } else {
      // show play button
      playPause.setImageResource(R.drawable.ic_play_circle_filled)
      playPause.setOnClickListener { play() }
    }

    // update current entry info
    val title = song_title
    val description = song_description
    val duration = song_duration
    val queuer = song_queuer

    val songEntry = playerState.songEntry
    if (songEntry == null) {
      title.text = ""
      description.text = ""
      duration.text = ""
      queuer.text = ""
      return
    }

    val song = songEntry.song
    if (title.text != song.title) {
      title.text = song.title
    }
    val songDescription = song.description
    if (description.text != songDescription) {
      description.text = songDescription
    }
    if (songDescription.isEmpty()) {
      description.visibility = View.GONE
    } else {
      description.visibility = View.VISIBLE
    }
    val durationSeconds = song.duration!!
    if (durationSeconds > 0) {
      duration.visibility = View.VISIBLE
      val seconds = durationSeconds % 60
      val minutes = (durationSeconds - seconds) / 60
      duration.text = String.format(Locale.US, "%d:%02d", minutes, seconds)
    } else {
      duration.visibility = View.GONE
    }

    val userName = songEntry.userName
    if (queuer.text != userName) {
      if (userName == null) {
        queuer.text = getString(R.string.suggested)
      } else {
        queuer.text = userName
      }
    }

    val albumArtUrl = song.albumArtUrl
    val albumArt = album_art
    Picasso.with(context)
        .load(albumArtUrl)
        .placeholder(R.drawable.ic_broken_image)
        .into(albumArt)
  }

  private inner class NextSwipeListener(ctx: Context) : OnTouchListener, OnGestureListener {

    private val gestureDetector: GestureDetectorCompat = GestureDetectorCompat(ctx, this)

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    override fun onTouch(v: View, event: MotionEvent): Boolean =
        gestureDetector.onTouchEvent(event)

    /**
     * Notified when a tap occurs with the down [MotionEvent]
     * that triggered it. This will be triggered immediately for
     * every down event. All other events should be preceded by this.
     *
     * @param e The down motion event.
     */
    override fun onDown(e: MotionEvent): Boolean = true

    /**
     * The userName has performed a down [MotionEvent] and not performed
     * a move or up yet. This event is commonly used to provide visual
     * feedback to the userName to let them know that their action has been
     * recognized i.e. highlight an element.
     *
     * @param e The down motion event
     */
    override fun onShowPress(e: MotionEvent) {}

    /**
     * Notified when a tap occurs with the up [MotionEvent]
     * that triggered it.
     *
     * @param e The up motion event that completed the first tap
     * @return true if the event is consumed, else false
     */
    override fun onSingleTapUp(e: MotionEvent): Boolean = true

    /**
     * Notified when a scroll occurs with the initial on down [MotionEvent] and the
     * current move [MotionEvent]. The distance in x and y is also supplied for
     * convenience.
     *
     * @param e1 The first down motion event that started the scrolling.
     * @param e2 The move motion event that triggered the current onScroll.
     * @param distanceX The distance along the X axis that has been scrolled since the last call to
     * onScroll. This is NOT the distance between `e1` and `e2`.
     * @param distanceY The distance along the Y axis that has been scrolled since the last call to
     * onScroll. This is NOT the distance between `e1` and `e2`.
     * @return true if the event is consumed, else false
     */
    override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float,
        distanceY: Float): Boolean =
        true

    /**
     * Notified when a long press occurs with the initial on down [MotionEvent]
     * that trigged it.
     *
     * @param e The initial on down motion event that started the longpress.
     */
    override fun onLongPress(e: MotionEvent) {}

    /**
     * Notified of a fling event when it occurs with the initial on down [MotionEvent]
     * and the matching up [MotionEvent]. The calculated velocity is supplied along
     * the x and y axis in pixels per second.
     *
     * @param e1 The first down motion event that started the fling.
     * @param e2 The move motion event that triggered the current onFling.
     * @param velocityX The velocity of this fling measured in pixels per second along the x axis.
     * @param velocityY The velocity of this fling measured in pixels per second along the y axis.
     * @return true if the event is consumed, else false
     */
    override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float,
        velocityY: Float): Boolean {
      var result = false
      val diffY = e2.y - e1.y
      val diffX = e2.x - e1.x
      if (Math.abs(diffX) > Math.abs(diffY)) {
        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
          if (diffX < 0) {
            onSwipeLeft()
          }
          result = true
        }
      }
      return result
    }

    private fun onSwipeLeft() {
      next()
    }
  }
}
