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
import com.github.bjoernpetersen.q.tag
import com.github.bjoernpetersen.q.ui.fragments.SearchFragment
import com.github.bjoernpetersen.q.ui.fragments.SongFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*
import java.lang.ref.WeakReference

class SearchActivity : AppCompatActivity(), SearchFragment.OnFragmentInteractionListener,
    SongFragment.OnListFragmentInteractionListener, ObserverUser {

  companion object {
    @JvmStatic
    val INITIAL_QUERY_EXTRA = SearchActivity::javaClass.name + ".initialQuery"
    @JvmStatic
    val INITIAL_PROVIDER_EXTRA = SearchActivity::javaClass.name + ".initialProvider"
  }

  override lateinit var observers: MutableList<WeakReference<Disposable>>
  private var query: String? = null

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

      override fun onPageSelected(position: Int) = refreshSearchResults(position)
      override fun onPageScrollStateChanged(state: Int) {}
    })

    query = intent.getStringExtra(INITIAL_QUERY_EXTRA)
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
      return
    }
    initObservers()
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
          updateProviders(it)
        }, {
          Log.d(tag(), "Could not retrieve providers", it)
          Toast.makeText(this, getString(R.string.no_provider_found), Toast.LENGTH_SHORT).show()
          finish()
        }).store()
  }

  private fun updateProviders(providers: List<NamedPlugin>) {
    view_pager?.adapter = SearchFragmentPagerAdapter(supportFragmentManager, providers)
    if (intent.hasExtra(INITIAL_PROVIDER_EXTRA)) {
      val initialId = intent.getStringExtra(INITIAL_PROVIDER_EXTRA)
      val index = providers
          .map(NamedPlugin::getId)
          .withIndex()
          .firstOrNull { id -> id.value == initialId }
          ?.index
      if (index == null) {
        Log.d(tag(), "Could not find initial provider")
      } else {
        view_pager.currentItem = index
      }
    }
    refreshSearchResults()
  }

  private fun refreshSearchResults() = view_pager?.apply { refreshSearchResults(currentItem) }

  private fun refreshSearchResults(position: Int) {
    query?.apply {
      (view_pager?.adapter as? SearchFragmentPagerAdapter)
          ?.getItem(position)
          ?.updateResults(this)
    }
  }

  override fun onAdd(song: Song, failCallback: () -> Unit) {
    Observable.fromCallable { Auth.apiKey.raw }
        .map { Connection.enqueue(it, song.id, song.provider.id) }
        .retry(1, {
          if (it is ApiException && it.code == 401) {
            Auth.clear(); true
          } else false
        })
        .doOnNext { QueueState.queue = it }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          Log.d(tag(), "Successfully added song to queue: ${song.title}")
        }, {
          Log.d(tag(), "Could not add a song.")
          failCallback()
          when (it) {
            is RegisterException -> if (it.reason == RegisterException.Reason.TAKEN) {
              Toast.makeText(
                  this,
                  "Your username is already taken.",
                  Toast.LENGTH_SHORT
              ).show()
              startActivity(Intent(this, LoginActivity::class.java))
            }
            is LoginException -> if (it.reason == LoginException.Reason.WRONG_UUID
                || it.reason == LoginException.Reason.WRONG_PASSWORD
                || it.reason == LoginException.Reason.NEEDS_AUTH) {
              Toast.makeText(
                  this,
                  "Can't login with current username and password.",
                  Toast.LENGTH_SHORT
              ).show()
              startActivity(Intent(this, LoginActivity::class.java))
            }
          }
        })
        .store()
  }

  override fun showAdd(song: Song): Boolean = !QueueState.queue.map { it.song }.any { it == song }
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