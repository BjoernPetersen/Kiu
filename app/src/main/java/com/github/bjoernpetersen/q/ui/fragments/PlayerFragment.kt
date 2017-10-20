package com.github.bjoernpetersen.q.ui.fragments


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.view.GestureDetectorCompat
import android.util.Log
import android.view.GestureDetector.OnGestureListener
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.github.bjoernpetersen.jmusicbot.client.ApiException
import com.github.bjoernpetersen.jmusicbot.client.model.PlayerState
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.Auth
import com.github.bjoernpetersen.q.api.AuthException
import com.github.bjoernpetersen.q.api.Connection
import com.github.bjoernpetersen.q.api.Permission
import com.github.bjoernpetersen.q.ui.runOnUiThread
import com.github.bjoernpetersen.q.ui.showToast
import com.squareup.picasso.Picasso
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

private val TAG = PlayerFragment::class.java.simpleName
private const val SWIPE_THRESHOLD = 100
private const val SWIPE_VELOCITY_THRESHOLD = 100

class PlayerFragment : Fragment() {

    private var updater: ScheduledExecutorService? = null
    private var updateTask: ScheduledFuture<*>? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater!!.inflate(R.layout.fragment_player, container, false)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val view = view ?: throw IllegalStateException()
        view.findViewById(R.id.song_title).isSelected = true
        view.findViewById(R.id.song_description).isSelected = true
        view.findViewById(R.id.song_queuer).isSelected = true
        view.setOnTouchListener(NextSwipeListener(context))
    }

    override fun onStart() {
        super.onStart()
        updater = Executors.newSingleThreadScheduledExecutor()
    }

    override fun onStop() {
        updater?.shutdown()
        updater = null
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        this.updateTask = updater?.scheduleWithFixedDelay(UpdateTask(), 0, 2, TimeUnit.SECONDS)
                ?: throw IllegalStateException("There is no updater for some reason")
    }

    override fun onPause() {
        super.onPause()
        this.updateTask?.cancel(true)
    }

    private fun pause() {
        updater?.submit {
            try {
                updatedState(Connection.pausePlayer())
            } catch (e: ApiException) {
                Log.w(TAG, "Could not pause player", e)
            }
        }
    }

    private fun play() {
        updater?.submit {
            try {
                updatedState(Connection.resumePlayer())
            } catch (e: ApiException) {
                Log.w(TAG, "Could not resume player", e)
            }
        }
    }

    private operator fun next() {
        val toast = Toast.makeText(
                context,
                R.string.skipping_current_song,
                Toast.LENGTH_SHORT
        )
        updater?.submit(Runnable {
            val connection = Connection
            if (!Auth.hasPermission(Permission.SKIP)) {
                return@Runnable
            }
            runOnUiThread(toast::show)
            try {
                val token = Auth.apiKey.raw
                updatedState(connection.nextSong(token))
            } catch (e: ApiException) {
                Auth.clear()
                Log.i(TAG, "Could not skip current song", e)
                runOnUiThread(toast::cancel)
                showToast(R.string.skip_error, Toast.LENGTH_SHORT)
            } catch (e: AuthException) {
                Log.wtf(TAG, e)
            }
        })
    }

    private fun updatedState(playerState: PlayerState) {
        val view = view ?: return
        view.post(Runnable {
            // update play/pause button
            val state = playerState.state
            val playPause = view.findViewById(R.id.play_pause) as ImageButton
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
            val title = view.findViewById(R.id.song_title) as TextView
            val description = view.findViewById(R.id.song_description) as TextView
            val duration = view.findViewById(R.id.song_duration) as TextView
            val queuer = view.findViewById(R.id.song_queuer) as TextView

            val songEntry = playerState.songEntry
            if (songEntry == null) {
                title.text = ""
                description.text = ""
                duration.text = ""
                queuer.text = ""
                return@Runnable
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
            val albumArt = view.findViewById(R.id.album_art) as ImageView
            Picasso.with(context)
                    .load(albumArtUrl)
                    .placeholder(R.drawable.ic_broken_image)
                    .into(albumArt)
        })
    }

    private inner class UpdateTask : Runnable {

        override fun run() {
            try {
                updatedState(Connection.getPlayerState())
            } catch (e: ApiException) {
                Log.v(javaClass.simpleName, "Error updating player fragment", e)
            }
        }

        private fun updatedState(playerState: PlayerState) {
            val view = view
            if (view != null) {
                this@PlayerFragment.updatedState(playerState)
            }
        }
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
        override fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean =
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
        override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
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
}// Required empty public constructor
