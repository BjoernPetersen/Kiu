package com.github.bjoernpetersen.q.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import com.github.bjoernpetersen.jmusicbot.client.model.NamedPlugin;
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.QueueState;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.api.Auth;
import com.github.bjoernpetersen.q.api.AuthException;
import com.github.bjoernpetersen.q.api.Config;
import com.github.bjoernpetersen.q.api.Connection;
import com.github.bjoernpetersen.q.ui.fragments.SongFragment;
import com.github.bjoernpetersen.q.ui.fragments.SuggestFragment;
import java.util.ArrayList;
import java.util.List;

public class SuggestActivity extends AppCompatActivity implements
    SuggestFragment.OnFragmentInteractionListener, SongFragment.OnListFragmentInteractionListener {

  private static final String TAG = SuggestActivity.class.getSimpleName();

  private ViewPager viewPager;
  private String query;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search);
    setTitle(R.string.title_search);

    ActionBar actionBar = getSupportActionBar();
    if (actionBar == null) {
      throw new IllegalStateException();
    }
    actionBar.setDisplayHomeAsUpEnabled(true);

    final ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
    this.viewPager = viewPager;
    viewPager.setPageTransformer(true, new RotateUpTransformer());
    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
      @Override
      public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
      }

      @Override
      public void onPageSelected(int position) {
        refreshSearchResults(position);
      }

      @Override
      public void onPageScrollStateChanged(int state) {
      }
    });

    loadSuggesters();
  }

  @Override
  protected void onDestroy() {
    viewPager = null;
    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.search_menu, menu);
    final MenuItem menuItem = menu.findItem(R.id.search_bar);
    SearchView searchView = (SearchView) menuItem.getActionView();
    searchView.setIconifiedByDefault(false);
    searchView.setQueryHint(getString(R.string.search_hint));
    View magView = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
    ((ViewGroup) magView.getParent()).removeView(magView);
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        query = newText;
        refreshSearchResults();
        return true;
      }
    });
    return true;
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    final MenuItem menuItem = menu.findItem(R.id.search_bar);
    SearchView searchView = (SearchView) menuItem.getActionView();
    searchView.setQuery(query, false);
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (!Config.INSTANCE.hasUser()) {
      startActivity(new Intent(this, LoginActivity.class));
    }
  }

  private void loadSuggesters() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          final List<NamedPlugin> providers = Connection.INSTANCE.getSuggesters();
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              updateSuggesters(providers);
            }
          });
        } catch (ApiException e) {
          Log.e(TAG, "Could not retrieve providers", e);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(
                  SuggestActivity.this,
                  getString(R.string.no_suggester_found),
                  Toast.LENGTH_SHORT
              ).show();
              finish();
            }
          });
        }
      }
    }, "SuggesterLoader").start();
  }

  private void updateSuggesters(List<NamedPlugin> suggesters) {
    if (viewPager != null) {
      viewPager
          .setAdapter(new SuggestFragmentPagerAdapter(getSupportFragmentManager(), suggesters));
    }
  }

  private void refreshSearchResults() {
    if (viewPager != null) {
      refreshSearchResults(viewPager.getCurrentItem());
    }
  }

  private void refreshSearchResults(int position) {
    PagerAdapter adapter = viewPager.getAdapter();
    if (adapter instanceof SuggestFragmentPagerAdapter) {
      SuggestFragmentPagerAdapter searchAdapter = (SuggestFragmentPagerAdapter) adapter;
      searchAdapter.getItem(position).update();
    }
  }

  @Override
  public void onAdd(final Song song) {
    new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          String token = Auth.INSTANCE.getApiKey().getRaw();
          List<QueueEntry> queueEntries = Connection.INSTANCE
              .enqueue(token, song.getId(), song.getProvider().getId());
          QueueState.getInstance().set(queueEntries);
        } catch (ApiException e) {
          e.printStackTrace();
        } catch (AuthException e) {
          Log.d(TAG, "Could not add song", e);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              startActivity(new Intent(SuggestActivity.this, LoginActivity.class));
            }
          });
        }
      }
    }, "enqueueThread").start();
  }

  @Override
  public boolean showAdd(Song song) {
    return true;
  }
}

final class SuggestFragmentPagerAdapter extends FragmentPagerAdapter {

  private final List<SuggestFragment> fragments;

  SuggestFragmentPagerAdapter(FragmentManager fm, List<NamedPlugin> suggesters) {
    super(fm);
    fragments = new ArrayList<>(suggesters.size());
    for (NamedPlugin provider : suggesters) {
      fragments.add(SuggestFragment.newInstance(provider));
    }
  }

  /**
   * Return the Fragment associated with a specified position.
   */
  @Override
  public SuggestFragment getItem(int position) {
    return fragments.get(position);
  }

  /**
   * Return the number of views available.
   */
  @Override
  public int getCount() {
    return fragments.size();
  }

  @Override
  public CharSequence getPageTitle(int position) {
    return getItem(position).getSuggester().getId();
  }
}