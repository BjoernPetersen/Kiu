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
import java.util.*

class SuggestActivity : AppCompatActivity(), SuggestFragment.OnFragmentInteractionListener,
    SongFragment.OnListFragmentInteractionListener {

  private var viewPager: ViewPager? = null

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

    loadSuggesters()
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
    }
  }

  override fun onResume() {
    super.onResume()
    checkWifiState(this)
  }

  private fun loadSuggesters() {
    Thread({
      try {
        val providers = Connection.getSuggesters()
        runOnUiThread { updateSuggesters(providers) }
      } catch (e: ApiException) {
        Log.v(tag(), "Could not retrieve providers", e)
        runOnUiThread {
          Toast.makeText(
              this,
              getString(R.string.no_suggester_found),
              Toast.LENGTH_SHORT
          ).show()
          finish()
        }
      }
    }, "SuggesterLoader").start()
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

  private fun enqueue(song: Song) {
    try {
      val token = Auth.apiKey.raw
      val queueEntries = Connection.enqueue(token, song.id, song.provider.id)
      QueueState.queue = queueEntries
    } catch (e: ApiException) {
      if (e.code == 401) {
        Auth.clear()
        Log.v(tag(), "Could not add song, trying again with cleared auth...")
        enqueue(song)
      } else {
        Log.d(tag(), "Could not add song: " + e.code, e)
      }
    } catch (e: RegisterException) {
      if (e.reason === RegisterException.Reason.TAKEN) {
        runOnUiThread {
          Toast.makeText(
              this@SuggestActivity,
              "Your username is already taken.",
              Toast.LENGTH_SHORT
          ).show()
          startActivity(Intent(this, LoginActivity::class.java))
        }
      }
    } catch (e: LoginException) {
      runOnUiThread { startActivity(Intent(this, LoginActivity::class.java)) }
    } catch (e: AuthException) {
      Log.d(tag(), "Could not add song", e)
    }
  }

  override fun onAdd(song: Song) = Thread({ enqueue(song) }, "enqueueThread").start()
  override fun showAdd(song: Song): Boolean = true
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