package com.github.bjoernpetersen.q.ui

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentManager
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
import com.github.bjoernpetersen.q.ui.fragments.CachedFragmentPagerAdapter
import com.github.bjoernpetersen.q.ui.fragments.SongFragment
import com.github.bjoernpetersen.q.ui.fragments.SuggestFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_search.*
import java.lang.ref.WeakReference

class SuggestActivity : AppCompatActivity(), SuggestFragment.OnFragmentInteractionListener,
    SongFragment.SongFragmentInteractionListener, ObserverUser {

  override lateinit var observers: MutableList<WeakReference<Disposable>>
  private var suggesters: List<NamedPlugin> = emptyList()
    set(value) {
      if (field != value) {
        field = value
        view_pager?.adapter = SuggestFragmentPagerAdapter(supportFragmentManager, value)
        supportFragmentManager.executePendingTransactions()
        refreshSuggestions()
      }
    }
  private val activeSuggester: NamedPlugin?
    get() {
      val index = view_pager.currentItem
      return if (index < suggesters.size) suggesters[index]
      else null
    }

  override fun initObservers() {
    observers = ArrayList()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_search)
    setTitle(R.string.title_suggestions)

    val actionBar = supportActionBar ?: throw IllegalStateException()
    actionBar.setDisplayHomeAsUpEnabled(true)

    view_pager.setPageTransformer(true, RotateUpTransformer())
    view_pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
      override fun onPageScrolled(position: Int, positionOffset: Float,
          positionOffsetPixels: Int) {
      }

      override fun onPageSelected(position: Int) = refreshSuggestions()
      override fun onPageScrollStateChanged(state: Int) {}
    })
  }

  override fun onDestroy() {
    view_pager.clearOnPageChangeListeners()
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
    checkWifiState()
  }

  private fun loadSuggesters() {
    Observable.fromCallable { Connection.getSuggesters() }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          suggesters = it
        }, {
          Log.d(tag(), "Could not retrieve suggesters", it)
          Toast.makeText(this, getString(R.string.no_suggester_found), Toast.LENGTH_SHORT).show()
          finish()
        }).store()
  }

  private fun refreshSuggestions() {
    (view_pager.adapter as? SuggestFragmentPagerAdapter)
        ?.getFragment(view_pager.currentItem)
        ?.refresh()
  }

  override fun onContextMenu(song: Song, menuItem: MenuItem, enable: (Boolean) -> Unit): Boolean {
    when (menuItem.itemId) {
      R.id.enqueue_button -> onClick(song, enable)
      R.id.remove_button -> dislike(song, enable)
    }
    return true
  }

  private fun dislike(song: Song, enable: (Boolean) -> Unit) {
    enable(false)
    val suggesterId = activeSuggester?.id ?: return
    Observable.fromCallable { Auth.apiKey.raw }
        .subscribeOn(Schedulers.io())
        .map { Connection.removeSuggestion(suggesterId, it, song.id, song.provider.id) }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          Log.d(tag(), "Successfully disliked song.")
        }, {
          enable(true)
          Toast.makeText(this, R.string.dislike_error, Toast.LENGTH_SHORT).show()
          Log.d(tag(), "Could not remove suggestion", it)
        })
        .store()
  }

  override fun onClick(song: Song, enable: (Boolean) -> Unit) {
    enable(false)
    Observable.fromCallable { Auth.apiKey.raw }
        .subscribeOn(Schedulers.io())
        .map { Connection.enqueue(it, song.id, song.provider.id) }
        .retry(1, {
          if (it is ApiException && it.code == 401) {
            Auth.clear(); true
          } else false
        })
        .doOnNext { QueueState.queue = it }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          Log.d(tag(), "Successfully added song to queue: ${song.title}")
        }, {
          Log.d(tag(), "Could not add a song.")
          enable(true)
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

  override fun isEnabled(song: Song): Boolean = !QueueState.queue.map { it.song }.any { it == song }

  override fun isEnabled(song: Song, menuItemId: Int): Boolean = when (menuItemId) {
    R.id.enqueue_button -> isEnabled(song)
    R.id.remove_button -> Auth.hasPermissionNoRefresh(Permission.DISLIKE)
    else -> true
  }
}

internal class SuggestFragmentPagerAdapter(fm: FragmentManager,
    private val suggesters: List<NamedPlugin>) :
    CachedFragmentPagerAdapter<SuggestFragment>(fm, suggesters.size) {

  /**
   * Return the Fragment associated with a specified position.
   */
  override fun getItem(position: Int): SuggestFragment =
      SuggestFragment.newInstance(suggesters[position])

  /**
   * Return the number of views available.
   */
  override fun getCount(): Int = suggesters.size

  override fun getPageTitle(position: Int): CharSequence = suggesters[position].name
}