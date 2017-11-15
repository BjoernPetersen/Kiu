package com.github.bjoernpetersen.q.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer
import com.github.bjoernpetersen.jmusicbot.client.ApiException
import com.github.bjoernpetersen.jmusicbot.client.model.NamedPlugin
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import com.github.bjoernpetersen.q.QueueState
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.*
import com.github.bjoernpetersen.q.ui.fragments.SearchFragment
import com.github.bjoernpetersen.q.ui.fragments.SongFragment
import java.util.*

private val TAG = SearchActivity::class.java.simpleName

class SearchActivity : AppCompatActivity(), SearchFragment.OnFragmentInteractionListener,
    SongFragment.OnListFragmentInteractionListener {

  private var viewPager: ViewPager? = null
  private var query: String? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_search)
    setTitle(R.string.title_search)

    val actionBar = supportActionBar ?: throw IllegalStateException()
    actionBar.setDisplayHomeAsUpEnabled(true)

    val viewPager: ViewPager = findViewById(R.id.view_pager)
    this.viewPager = viewPager
    viewPager.setPageTransformer(true, RotateUpTransformer())
    viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrolled(position: Int, positionOffset: Float,
          positionOffsetPixels: Int) {
      }

      override fun onPageSelected(position: Int) = refreshSearchResults(position)
      override fun onPageScrollStateChanged(state: Int) {}
    })

    loadProviders()
  }

  override fun onDestroy() {
    viewPager = null
    super.onDestroy()
  }

  override fun onResume() {
    super.onResume()
    checkWifiState(this)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean = true.also {
    val inflater = menuInflater
    inflater.inflate(R.menu.search_menu, menu)
    val menuItem = menu.findItem(R.id.search_bar)
    val searchView = menuItem.actionView as SearchView
    searchView.setIconifiedByDefault(false)
    searchView.queryHint = getString(R.string.search_hint)
    val magView: View = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon)
    (magView.parent as ViewGroup).removeView(magView)
    searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
      override fun onQueryTextSubmit(query: String): Boolean = true
      override fun onQueryTextChange(newText: String): Boolean = true.also {
        query = newText
        refreshSearchResults()
      }
    })
  }

  override fun onPrepareOptionsMenu(menu: Menu): Boolean {
    val menuItem = menu.findItem(R.id.search_bar)
    val searchView = menuItem.actionView as SearchView
    searchView.setQuery(query, false)
    return super.onPrepareOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    android.R.id.home -> {
      finish()
      true
    }
    else -> super.onOptionsItemSelected(item)
  }

  override fun onStart() {
    super.onStart()
    if (!Config.hasUser()) {
      startActivity(Intent(this, LoginActivity::class.java))
    }
  }

  private fun loadProviders() {
    Thread({
      try {
        val providers = Connection.getProviders()
        runOnUiThread { updateProviders(providers) }
      } catch (e: ApiException) {
        Log.v(TAG, "Could not retrieve providers", e)
        runOnUiThread {
          Toast.makeText(
              this,
              getString(R.string.no_provider_found),
              Toast.LENGTH_SHORT
          ).show()
          finish()
        }
      }
    }, "ProviderLoader").start()
  }

  private fun updateProviders(providers: List<NamedPlugin>) {
    viewPager?.adapter = SearchFragmentPagerAdapter(supportFragmentManager, providers)
  }

  private fun refreshSearchResults() = viewPager?.apply { refreshSearchResults(currentItem) }

  private fun refreshSearchResults(position: Int) {
    query?.apply {
      (viewPager?.adapter as? SearchFragmentPagerAdapter)
          ?.getItem(position)
          ?.updateResults(this)
    }
  }

  private fun enqueue(song: Song) {
    try {
      val token = Auth.apiKey.raw
      val queueEntries = Connection.enqueue(token, song.id, song.provider.id)
      QueueState.queue = queueEntries
    } catch (e: ApiException) {
      if (e.code == 401) {
        Log.v(TAG, "Could not add song, trying again with cleared auth...")
        Auth.clear()
        enqueue(song)
      } else Log.d(TAG, "Couldn't add song to queue. (${e.code})", e)
    } catch (e: RegisterException) {
      if (e.reason === RegisterException.Reason.TAKEN) {
        runOnUiThread { startActivity(Intent(this, LoginActivity::class.java)) }
      }
    } catch (e: LoginException) {
      runOnUiThread { startActivity(Intent(this, LoginActivity::class.java)) }
    } catch (e: AuthException) {
      Log.d(TAG, "Could not add song", e)
    }
  }

  override fun onAdd(song: Song) {
    Thread({ enqueue(song) }, "enqueueThread").start()
  }

  override fun showAdd(song: Song): Boolean = true
}

internal class SearchFragmentPagerAdapter(fm: FragmentManager, providers: List<NamedPlugin>) :
    FragmentPagerAdapter(fm) {

  private val fragments: MutableList<SearchFragment>

  init {
    fragments = ArrayList(providers.size)
    providers.mapTo(fragments) { SearchFragment.newInstance(it) }
  }

  /**
   * Return the Fragment associated with a specified position.
   */
  override fun getItem(position: Int): SearchFragment = fragments[position]

  /**
   * Return the number of views available.
   */
  override fun getCount(): Int = fragments.size

  override fun getPageTitle(position: Int): CharSequence? = getItem(position).provider?.name
}