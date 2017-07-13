package com.github.bjoernpetersen.q.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.api.Connection;
import com.github.bjoernpetersen.q.ui.fragments.PlayerFragment;
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryAddButtonsDataBinder.QueueEntryAddButtonsListener;
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryDataBinder.QueueEntryListener;
import com.github.bjoernpetersen.q.ui.fragments.QueueFragment;
import io.sentry.Sentry;
import io.sentry.android.AndroidSentryClientFactory;

public class MainActivity extends AppCompatActivity implements
    QueueEntryListener, QueueEntryAddButtonsListener {

  private static final String TAG = MainActivity.class.getSimpleName();
  private static final String SENTRY_DSN = "https://ab694f1a00ae41678d673c676de4bb9e:cd17682534a746e2bdabbf909568b7fe@sentry.io/186487";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Sentry.init(SENTRY_DSN, new AndroidSentryClientFactory(getApplicationContext()));

    setContentView(R.layout.activity_main);
    setTitle(getString(R.string.queue));

    getSupportFragmentManager().beginTransaction()
        .add(R.id.current_song, new PlayerFragment())
        .add(R.id.song_list, QueueFragment.newInstance())
        .commit();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);

    if (!Connection.get(this).isFullUser()) {
      menu.findItem(R.id.upgrade).setVisible(true);
    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.upgrade:
        upgrade(item);
        return true;
      case R.id.logout:
        Connection.get(this).reset();
        startActivity(new Intent(this, LoginActivity.class));
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void upgrade(final MenuItem upgradeItem) {
    if (!Connection.get(this).isFullUser()) {
      final EditText editText = new EditText(this);
      editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

      new AlertDialog.Builder(this)
          .setTitle(R.string.enter_your_password)
          .setView(editText)
          .setCancelable(true)
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              upgrade(upgradeItem, editText.getText().toString());
            }
          })
          .show();
    }
  }

  private void upgrade(final MenuItem upgradeItem, final String password) {
    if (password.isEmpty()) {
      Toast.makeText(this, R.string.error_empty, Toast.LENGTH_SHORT).show();
      upgrade(upgradeItem);
    }

    final Context context = this;
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          Connection.get(context).upgrade(password);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(context, R.string.upgrade_success, Toast.LENGTH_SHORT).show();
              upgradeItem.setVisible(false);
            }
          });
        } catch (final ApiException e) {
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              switch (e.getCode()) {
                case 400:
                  Toast.makeText(context, R.string.invalid_password, Toast.LENGTH_SHORT).show();
                  upgrade(upgradeItem);
                  break;
                case 401:
                case 403:
                  Toast.makeText(context, R.string.invalid_credentials, Toast.LENGTH_LONG).show();
                  break;
                default:
                  Log.e(TAG, "Unknown upgrade error", e);
                  Toast.makeText(context, getString(R.string.unknown_error, e.getCode()),
                      Toast.LENGTH_SHORT).show();
              }
            }
          });
        }
      }
    }, "upgradeThread").start();

  }

  @Override
  public void onSearchClick() {
    Intent intent = new Intent(this, SearchActivity.class);
    startActivity(intent);
  }

  @Override
  public void onSuggestionsClick() {
    Intent intent = new Intent(this, SuggestActivity.class);
    startActivity(intent);
  }

  @Override
  public void onClick(QueueEntry entry) {

  }
}
