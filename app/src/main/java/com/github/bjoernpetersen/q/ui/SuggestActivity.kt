package com.github.bjoernpetersen.q.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer
import com.github.bjoernpetersen.jmusicbot.client.ApiException
import com.github.bjoernpetersen.jmusicbot.client.model.NamedPlugin
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import com.github.bjoernpetersen.q.QueueState
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.*
import com.github.bjoernpetersen.q.tag
import com.github.bjoernpetersen.q.ui.fragments.SongFragment
import com.github.bjoernpetersen.q.ui.fragments.SuggestFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.lang.ref.WeakReference

class SuggestActivity : AppCompatActivity(), SuggestFragment.OnFragmentInteractionListener,
    SongFragment.OnListFragmentInteractionListener, ObserverUser {

  override lateinit var observers: MutableList<WeakReference<Disposable>>
  private var viewPager: ViewPager? = null

  override fun initObservers() {
    observers = ArrayList()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_search)
    setTitle(R.string.title_suggestions)

    val actionBar = supportActionBar ?: throw IllegalStateException()
    actionBar.setDisplayHomeAsUpEnabled(true)

    val viewPager: ViewPager = findViewById(R.id.view_pager)
    this.viewPager = viewPager
    viewPager.setPageTransformer(true, RotateUpTransformer())
    viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrolled(position: Int, positionOffset: Float,
          positionOffsetPixels: Int) {
      }

      override fun onPageSelected(position: Int) = refreshSuggestions(position)
      override fun onPageScrollStateChanged(state: Int) {}
    })
  }

  override fun onDestroy() {
    viewPager = null
    super.onDestroy()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean = true.also {
    menuInflater.inflate(R.menu.suggest_menu, menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    android.R.id.home -> {
      finish()
      true
    }
    R.id.refresh -> {
      refreshSuggestions()
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
    loadSuggesters()
  }

  override fun onStop() {
    disposeObservers()
    super.onStop()
  }

  override fun onResume() {
    super.onResume()
    checkWifiState(this)
  }

  private fun loadSuggesters() {
    Observable.fromCallable { Connection.getSuggesters() }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          updateSuggesters(it)
        }, {
          Log.d(tag(), "Could not retrieve suggesters", it)
          Toast.makeText(this, getString(R.string.no_suggester_found), Toast.LENGTH_SHORT).show()
          finish()
        }).store()
  }

  private fun updateSuggesters(suggesters: List<NamedPlugin>) {
    viewPager?.adapter = SuggestFragmentPagerAdapter(supportFragmentManager, suggesters)
  }

  private fun refreshSuggestions() = viewPager?.apply {
    refreshSuggestions(currentItem)
  }

  private fun refreshSuggestions(position: Int) {
    (viewPager?.adapter as? SuggestFragmentPagerAdapter)?.getItem(position)?.update()
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

internal class SuggestFragmentPagerAdapter(fm: FragmentManager, suggesters: List<NamedPlugin>) :
    FragmentPagerAdapter(fm) {

  private val fragments: MutableList<SuggestFragment>
  private val titles: MutableList<String>

  init {
    fragments = ArrayList(suggesters.size)
    titles = ArrayList(suggesters.size)
    for (suggester in suggesters) {
      fragments.add(SuggestFragment.newInstance(suggester))
      titles.add(suggester.name)
    }
  }

  /**
   * Return the Fragment associated with a specified position.
   */
  override fun getItem(position: Int): SuggestFragment = fragments[position]

  /**
   * Return the number of views available.
   */
  override fun getCount(): Int = fragments.size

  override fun getPageTitle(position: Int): CharSequence = titles[position]
}