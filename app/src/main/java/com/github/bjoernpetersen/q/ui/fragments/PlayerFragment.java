package com.github.bjoernpetersen.q.ui.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import com.github.bjoernpetersen.jmusicbot.client.model.PlayerState;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.jmusicbot.client.model.SongEntry;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.api.Connection;
import com.squareup.picasso.Picasso;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class PlayerFragment extends Fragment {

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

  private class UpdateTask implements Runnable {

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

    @Override
    public void run() {
      try {
        updatedState(Connection.get(getContext()).getPlayerState());
      } catch (ApiException e) {
        Log.w(getClass().getSimpleName(), "Error updating player fragment", e);
      }
    }

    private void updatedState(@NonNull final PlayerState playerState) {
      final View view = getView();
      if (view != null) {
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
    }
  }
}
