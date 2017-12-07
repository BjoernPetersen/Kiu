package com.github.bjoernpetersen.q.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.FragmentManager
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
import com.github.bjoernpetersen.jmusicbot.client.model.NamedPlugin
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import com.github.bjoernpetersen.q.QueueState
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.Config
import com.github.bjoernpetersen.q.api.Connection
import com.github.bjoernpetersen.q.api.action.Enqueue
import com.github.bjoernpetersen.q.tag
import com.github.bjoernpetersen.q.ui.fragments.CachedFragmentPagerAdapter
import com.github.bjoernpetersen.q.ui.fragments.SearchFragment
import com.github.bjoernpetersen.q.ui.fragments.SongFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*
import java.lang.ref.WeakReference

class SearchActivity : AppCompatActivity(), SearchFragment.OnFragmentInteractionListener,
    SongFragment.SongFragmentInteractionListener, ObserverUser {

  companion object {
    @JvmStatic
    private val LAST_PROVIDER_STATE = SearchActivity::javaClass.name + ".lastProvider"
    @JvmStatic
    val INITIAL_QUERY_EXTRA = SearchActivity::javaClass.name + ".initialQuery"
    @JvmStatic
    val INITIAL_PROVIDER_EXTRA = SearchActivity::javaClass.name + ".initialProvider"
  }

  override lateinit var observers: MutableList<WeakReference<Disposable>>
  private var preferredProvider: String? = null
    set(value) {
      field = value
      getPreferences(Context.MODE_PRIVATE).edit()
          .putString(LAST_PROVIDER_STATE, value)
          .apply()
    }
  private var query: String? = null
  private var providers: List<NamedPlugin> = emptyList()
    set(value) {
      if (field != value) {
        field = value
        view_pager?.adapter = SearchFragmentPagerAdapter(supportFragmentManager, value)
        supportFragmentManager.executePendingTransactions()
        if (preferredProvider != null) {
          val index = providers
              .map(NamedPlugin::getId)
              .withIndex()
              .firstOrNull { id -> id.value == preferredProvider }
              ?.index
          if (index == null) {
            Log.d(tag(), "Could not find initial provider")
          } else {
            view_pager.currentItem = index
          }
        }
        refreshSearchResults()
      }
    }

  override fun initObservers() {
    observers = ArrayList()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_search)
    setTitle(R.string.title_search)

    val actionBar = supportActionBar ?: throw IllegalStateException()
    actionBar.setDisplayHomeAsUpEnabled(true)

    val viewPager: ViewPager = view_pager
    viewPager.setPageTransformer(true, RotateUpTransformer())
    viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrolled(position: Int, positionOffset: Float,
          positionOffsetPixels: Int) {
      }

      override fun onPageSelected(position: Int) = refreshSearchResults()
      override fun onPageScrollStateChanged(state: Int) {}
    })

    query = intent.getStringExtra(INITIAL_QUERY_EXTRA)
    preferredProvider = intent.getStringExtra(INITIAL_PROVIDER_EXTRA) ?:
        getPreferences(Context.MODE_PRIVATE).getString(LAST_PROVIDER_STATE, null)
  }

  override fun onDestroy() {
    view_pager.clearOnPageChangeListeners()
    super.onDestroy()
  }

  override fun onResume() {
    super.onResume()
    checkWifiState()
  }

  override fun onPause() {
    val currentIndex = view_pager.currentItem
    if (currentIndex < providers.size)
      preferredProvider = providers[currentIndex].id
    super.onPause()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean = true.also {
    val inflater = menuInflater
    inflater.inflate(R.menu.search_menu, menu)

    // hide search icon
    val menuItem = menu.findItem(R.id.search_bar)
    val searchView = menuItem.actionView as SearchView
    searchView.setIconifiedByDefault(false)
    val magView: View = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon)
    (magView.parent as ViewGroup).removeView(magView)

    searchView.queryHint = getString(R.string.search_hint)
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
    showKeyboard(searchView)
    return super.onPrepareOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    android.R.id.home -> {
      hideKeyboard()
      finish()
      true
    }
    else -> super.onOptionsItemSelected(item)
  }

  override fun onStart() {
    super.onStart()
    initObservers()
    if (!Config.hasUser()) {
      startActivity(Intent(this, LoginActivity::class.java))
      return
    }
    loadProviders()
  }

  override fun onStop() {
    disposeObservers()
    super.onStop()
  }

  private fun loadProviders() {
    Observable.fromCallable { Connection.getProviders() }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          providers = it
        }, {
          Log.d(tag(), "Could not retrieve providers", it)
          Toast.makeText(this, getString(R.string.no_provider_found), Toast.LENGTH_SHORT).show()
          finish()
        }).store()
  }

  private fun refreshSearchResults() {
    query?.apply {
      (view_pager?.adapter as? SearchFragmentPagerAdapter)
          ?.getFragment(view_pager.currentItem)
          ?.updateResults(this)
    }
  }

  override fun onContextMenu(song: Song, menuItem: MenuItem,
      enable: (Boolean) -> Unit): Boolean = true.also { onClick(song, enable) }

  override fun onClick(song: Song, enable: (Boolean) -> Unit) {
    Enqueue(song)
        .defaultAction(this, enable)
        .store()
  }

  override fun isEnabled(song: Song): Boolean = !QueueState.queue.map { it.song }.any { it == song }

  override fun isEnabled(song: Song, menuItemId: Int): Boolean = when (menuItemId) {
    R.id.enqueue_button -> isEnabled(song)
    else -> true
  }

  override fun onSongListTouch() {
    hideKeyboard()
  }
}

internal class SearchFragmentPagerAdapter(fm: FragmentManager,
    private val providers: List<NamedPlugin>) :
    CachedFragmentPagerAdapter<SearchFragment>(fm, providers.size) {

  /**
   * Return the Fragment associated with a specified position.
   */
  override fun getItem(position: Int): SearchFragment =
      SearchFragment.newInstance(providers[position])

  /**
   * Return the number of views available.
   */
  override fun getCount(): Int = providers.size

  override fun getPageTitle(position: Int): CharSequence? = providers[position].name

  override fun saveState(): Parcelable? = null
}
