package com.github.bjoernpetersen.q.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.ui.fragments.PlayerFragment;
import com.github.bjoernpetersen.q.ui.fragments.QueueFragment;

public class MainActivity extends AppCompatActivity implements
    QueueFragment.ListFragmentInteractionListener {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) {
      throw new IllegalStateException("No action bar");
    }
    actionBar.setTitle(getString(R.string.queue));

    getSupportFragmentManager().beginTransaction()
        .add(R.id.current_song, new PlayerFragment())
        .add(R.id.song_list, QueueFragment.newInstance())
        .commit();
  }

  @Override
  protected void onStart() {
    super.onStart();

  }

  @Override
  public void onSearchClick() {

  }

  @Override
  public void onSuggestClick() {

  }

  @Override
  public void onSongClick(Song song) {

  }
}
