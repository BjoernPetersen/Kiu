package com.github.bjoernpetersen.q.ui

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import com.github.bjoernpetersen.q.QueueState
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.action.Enqueue
import com.github.bjoernpetersen.q.star.StarredAccess
import com.github.bjoernpetersen.q.tag
import com.github.bjoernpetersen.q.ui.fragments.EnableCallback
import com.github.bjoernpetersen.q.ui.fragments.LoadingFragment
import com.github.bjoernpetersen.q.ui.fragments.SongFragment
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference

class StarActivity : AppCompatActivity(), ObserverUser,
    SongFragment.SongFragmentInteractionListener {

  private var access: StarredAccess? = null
  override var observers: MutableList<WeakReference<Disposable>> = ArrayList(0)

  override fun initObservers() {
    observers = ArrayList(2)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_star)
    setTitle(R.string.title_star)

    val actionBar = supportActionBar ?: throw IllegalStateException()
    actionBar.setDisplayHomeAsUpEnabled(true)
  }

  override fun onStart() {
    super.onStart()
    initObservers()

    supportFragmentManager.beginTransaction()
        .add(R.id.song_list, LoadingFragment())
        .commit()
    access = StarredAccess.instance(applicationContext).also {
      it.getAll().subscribe({
        supportFragmentManager.beginTransaction()
            .replace(R.id.song_list, SongFragment.newInstance(it, R.menu.star_context_menu))
            .commit()
      }, {
        Log.d(tag(), "Error getting songs", it)
      }).store()
    }
  }

  override fun onStop() {
    disposeObservers()
    access = null
    super.onStop()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    android.R.id.home -> {
      finish()
      true
    }
    else -> super.onOptionsItemSelected(item)
  }

  private fun searchGoogle(query: String) {
    val intent = Intent(Intent.ACTION_WEB_SEARCH)
    intent.putExtra(SearchManager.QUERY, query)
    try {
      startActivity(intent)
    } catch (e: ActivityNotFoundException) {
      // happens if the Google app is deactivated
      val encodedQuery = Uri.encode(query)
      val uri = Uri.parse("http://www.google.com/#q=$encodedQuery")
      val browserIntent = Intent(Intent.ACTION_VIEW, uri)
      startActivity(browserIntent)
    }
  }

  override fun onContextMenu(song: Song, menuItem: MenuItem,
      enable: EnableCallback): Boolean = when (menuItem.itemId) {
    R.id.enqueue_button -> true.also { onClick(song, enable) }
    R.id.google -> true.also { searchGoogle(song.title) }
    R.id.google_specific -> true.also { searchGoogle("${song.title} ${song.description}") }
    R.id.unstar -> true.also {
      access?.remove(song)?.subscribe({
        Log.d(tag(), "Unstarred song: ${song.title}")
      }, {
        Log.w(tag(), "Remove error for song ${song.title}", it)
        Toast.makeText(this, R.string.unstar_error, Toast.LENGTH_SHORT).show()
      })
    }
    else -> false
  }

  override fun onClick(song: Song, enable: EnableCallback) {
    Enqueue(song)
        .defaultAction(this, enable)
        .store()
  }

  override fun isEnabled(song: Song): Boolean = QueueState.queue
      .map { it.song }
      .none { it.id == song.id && it.provider.id == song.provider.id }

  override fun isEnabled(song: Song, menuItemId: Int): Boolean = true
}

