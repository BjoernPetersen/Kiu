package com.github.bjoernpetersen.q.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GestureDetectorCompat;
import android.util.Log;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import com.github.bjoernpetersen.jmusicbot.client.model.PlayerState;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.jmusicbot.client.model.SongEntry;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.api.Connection;
import com.github.bjoernpetersen.q.api.Permission;
import com.squareup.picasso.Picasso;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment {

  private static final String TAG = PlayerFragment.class.getSimpleName();

  private ScheduledExecutorService updater;
  private ScheduledFuture<?> updateTask;

  public PlayerFragment() {
    // Required empty public constructor
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_player, container, false);
  }


  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    View view = getView();
    if (view == null) {
      // should never happen
      return;
    }
    view.findViewById(R.id.song_title).setSelected(true);
    view.findViewById(R.id.song_description).setSelected(true);
    view.findViewById(R.id.song_queuer).setSelected(true);
    view.setOnTouchListener(new NextSwipeListener(getContext()));
  }

  @Override
  public void onStart() {
    super.onStart();
    updater = Executors.newSingleThreadScheduledExecutor();
  }

  @Override
  public void onStop() {
    updater.shutdown();
    updater = null;
    super.onStop();
  }

  @Override
  public void onResume() {
    super.onResume();
    this.updateTask = updater.scheduleWithFixedDelay(new UpdateTask(), 0, 2, TimeUnit.SECONDS);
  }

  @Override
  public void onPause() {
    super.onPause();
    this.updateTask.cancel(true);
  }

  private void pause() {
    updater.submit(new Runnable() {
      @Override
      public void run() {
        try {
          updatedState(Connection.get(getContext()).pausePlayer());
        } catch (ApiException e) {
          Log.w(getClass().getSimpleName(), "Could not pause player", e);
        }
      }
    });
  }

  private void play() {
    updater.submit(new Runnable() {
      @Override
      public void run() {
        try {
          updatedState(Connection.get(getContext()).resumePlayer());
        } catch (ApiException e) {
          Log.w(getClass().getSimpleName(), "Could not resume player", e);
        }
      }
    });
  }

  private void next() {
    final Toast toast = Toast.makeText(
        getContext(),
        R.string.skipping_current_song,
        Toast.LENGTH_SHORT
    );
    updater.submit(new Runnable() {
      @Override
      public void run() {
        Connection connection = Connection.get(getContext());
        if (!connection.checkHasPermission(Permission.SKIP)) {
          return;
        }
        Util.showToast(PlayerFragment.this, toast);
        try {
          updatedState(connection.nextSong());
        } catch (ApiException e) {
          connection.invalidateToken();
          Log.w(TAG, "Could not skip current song", e);
          Util.hideToast(PlayerFragment.this, toast);
          Util.showToast(PlayerFragment.this, R.string.skip_error, Toast.LENGTH_SHORT);
        }
      }
    });
  }

  private void updatedState(@NonNull final PlayerState playerState) {
    final View view = getView();
    if (view == null) {
      return;
    }
    view.post(new Runnable() {
      @Override
      public void run() {
        // update play/pause button
        PlayerState.StateEnum state = playerState.getState();
        ImageButton playPause = (ImageButton) view.findViewById(R.id.play_pause);
        if (state == PlayerState.StateEnum.PLAY) {
          // show pause button
          playPause.setImageResource(R.drawable.ic_pause_circle_filled);
          playPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              pause();
            }
          });
        } else {
          // show play button
          playPause.setImageResource(R.drawable.ic_play_circle_filled);
          playPause.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              play();
            }
          });
        }

        // update current entry info
        TextView title = (TextView) view.findViewById(R.id.song_title);
        TextView description = (TextView) view.findViewById(R.id.song_description);
        TextView queuer = (TextView) view.findViewById(R.id.song_queuer);

        SongEntry songEntry = playerState.getSongEntry();
        if (songEntry == null) {
          title.setText("");
          description.setText("");
          queuer.setText("");
          return;
        }

        Song song = songEntry.getSong();
        if (!title.getText().equals(song.getTitle())) {
          title.setText(song.getTitle());
        }
        String songDescription = song.getDescription();
        if (!description.getText().equals(songDescription)) {
          description.setText(songDescription);
        }
        if (songDescription.isEmpty()) {
          description.setVisibility(View.GONE);
        } else {
          description.setVisibility(View.VISIBLE);
        }

        String userName = songEntry.getUserName();
        if (!queuer.getText().equals(userName)) {
          if (userName == null) {
            queuer.setText(getString(R.string.suggested));
          } else {
            queuer.setText(userName);
          }
        }

        String albumArtUrl = song.getAlbumArtUrl();
        ImageView albumArt = (ImageView) view.findViewById(R.id.album_art);
        Picasso.with(getContext())
            .load(albumArtUrl)
            .placeholder(R.drawable.ic_broken_image)
            .into(albumArt);
      }
    });
  }

  private class UpdateTask implements Runnable {

    @Override
    public void run() {
      try {
        updatedState(Connection.get(getContext()).getPlayerState());
      } catch (ApiException e) {
        Log.v(getClass().getSimpleName(), "Error updating player fragment", e);
      }
    }

    private void updatedState(@NonNull final PlayerState playerState) {
      final View view = getView();
      if (view != null) {
        PlayerFragment.this.updatedState(playerState);
      }
    }
  }

  private class NextSwipeListener implements OnTouchListener, OnGestureListener {

    private static final int SWIPE_THRESHOLD = 100;
    private static final int SWIPE_VELOCITY_THRESHOLD = 100;

    private final GestureDetectorCompat gestureDetector;

    public NextSwipeListener(Context ctx) {
      this.gestureDetector = new GestureDetectorCompat(ctx, this);
    }

    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      return gestureDetector.onTouchEvent(event);
    }

    /**
     * Notified when a tap occurs with the down {@link MotionEvent}
     * that triggered it. This will be triggered immediately for
     * every down event. All other events should be preceded by this.
     *
     * @param e The down motion event.
     */
    @Override
    public boolean onDown(MotionEvent e) {
      return true;
    }

    /**
     * The user has performed a down {@link MotionEvent} and not performed
     * a move or up yet. This event is commonly used to provide visual
     * feedback to the user to let them know that their action has been
     * recognized i.e. highlight an element.
     *
     * @param e The down motion event
     */
    @Override
    public void onShowPress(MotionEvent e) {

    }

    /**
     * Notified when a tap occurs with the up {@link MotionEvent}
     * that triggered it.
     *
     * @param e The up motion event that completed the first tap
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
      return true;
    }

    /**
     * Notified when a scroll occurs with the initial on down {@link MotionEvent} and the
     * current move {@link MotionEvent}. The distance in x and y is also supplied for
     * convenience.
     *
     * @param e1 The first down motion event that started the scrolling.
     * @param e2 The move motion event that triggered the current onScroll.
     * @param distanceX The distance along the X axis that has been scrolled since the last call to
     * onScroll. This is NOT the distance between {@code e1} and {@code e2}.
     * @param distanceY The distance along the Y axis that has been scrolled since the last call to
     * onScroll. This is NOT the distance between {@code e1} and {@code e2}.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
      return true;
    }

    /**
     * Notified when a long press occurs with the initial on down {@link MotionEvent}
     * that trigged it.
     *
     * @param e The initial on down motion event that started the longpress.
     */
    @Override
    public void onLongPress(MotionEvent e) {

    }

    /**
     * Notified of a fling event when it occurs with the initial on down {@link MotionEvent}
     * and the matching up {@link MotionEvent}. The calculated velocity is supplied along
     * the x and y axis in pixels per second.
     *
     * @param e1 The first down motion event that started the fling.
     * @param e2 The move motion event that triggered the current onFling.
     * @param velocityX The velocity of this fling measured in pixels per second along the x axis.
     * @param velocityY The velocity of this fling measured in pixels per second along the y axis.
     * @return true if the event is consumed, else false
     */
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
      boolean result = false;
      float diffY = e2.getY() - e1.getY();
      float diffX = e2.getX() - e1.getX();
      if (Math.abs(diffX) > Math.abs(diffY)) {
        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
          if (diffX < 0) {
            onSwipeLeft();
          }
          result = true;
        }
      }
      return result;
    }

    private void onSwipeLeft() {
      next();
    }
  }
}
