package com.github.bjoernpetersen.q.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.github.bjoernpetersen.jmusicbot.client.ApiCallback;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import com.github.bjoernpetersen.jmusicbot.client.api.DefaultApi;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.api.Connection;
import com.github.bjoernpetersen.q.ui.fragments.PlayerFragment;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportFragmentManager().beginTransaction()
        .add(R.id.current_song, new PlayerFragment())
        .commit();
  }

  @Override
  protected void onStart() {
    super.onStart();

  }
}
