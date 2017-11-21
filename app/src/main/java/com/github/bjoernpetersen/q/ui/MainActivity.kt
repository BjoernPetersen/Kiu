package com.github.bjoernpetersen.q.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.*
import com.github.bjoernpetersen.q.api.action.MoveSong
import com.github.bjoernpetersen.q.tag
import com.github.bjoernpetersen.q.ui.fragments.PlayerFragment
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryAddButtonsDataBinder.QueueEntryAddButtonsListener
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryDataBinder.QueueEntryListener
import com.github.bjoernpetersen.q.ui.fragments.QueueFragment
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.exceptions.UndeliverableException
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import io.sentry.Sentry
import io.sentry.android.AndroidSentryClientFactory

private val TAG = MainActivity::class.java.simpleName
private const val SENTRY_DSN = "https://ab694f1a00ae41678d673c676de4bb9e:cd17682534a746e2bdabbf909568b7fe@sentry.io/186487"

class MainActivity : AppCompatActivity(), QueueEntryListener, QueueEntryAddButtonsListener {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Sentry.init(SENTRY_DSN, AndroidSentryClientFactory(applicationContext))
    Config.init(this)
    RxJavaPlugins.setErrorHandler {
      when (it) {
        is UndeliverableException -> Log.d(tag(), "Undeliverable error", it)
        else -> Log.w(tag(), "Uncaught error", it)
      }
    }
    Thread({
      try {
        Auth.apiKey
      } catch (e: AuthException) {
        Log.v(TAG, "Initial auth key retrieval failed...")
      } catch (e: UnknownAuthException) {
        Log.d(TAG, "Unknown auth exception", e)
      }
    }).start()

    setContentView(R.layout.activity_main)
    title = getString(R.string.queue)

    supportFragmentManager.beginTransaction()
        .add(R.id.current_song, PlayerFragment())
        .add(R.id.song_list, QueueFragment.newInstance())
        .commit()
  }

  override fun onResume() {
    super.onResume()
    checkWifiState(this)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.main_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onPrepareOptionsMenu(menu: Menu): Boolean {
    val upgrade = menu.findItem(R.id.upgrade)
    upgrade.isVisible = !Config.hasPassword()
    return super.onPrepareOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
    R.id.upgrade -> {
      upgrade()
      true
    }
    R.id.refresh_permissions -> {
      val dialog = AlertDialog.Builder(this)
          .setTitle(R.string.refreshing_permissions)
          .setView(ProgressBar(this).apply { animate() })
          .setCancelable(false)
          .show()
      Auth.clear()
      Observable.fromCallable { Auth.apiKey }
          .subscribeOn(Schedulers.io())
          .doOnError { Auth.clear() }
          .retry(1)
          .observeOn(AndroidSchedulers.mainThread())
          .doOnTerminate { dialog.dismiss() }
          .subscribe({
            Toast.makeText(this, R.string.refresh_success, Toast.LENGTH_LONG).show()
          }, {
            Log.d(tag(), "Could not refresh API key", it)
            Toast.makeText(this, R.string.refresh_permissions_error, Toast.LENGTH_LONG).show()
          })
      true
    }
    R.id.logout -> {
      Config.reset()
      startActivity(Intent(this, LoginActivity::class.java))
      true
    }
    else -> super.onOptionsItemSelected(item)
  }

  private fun upgrade() {
    if (!Config.hasPassword()) {
      val editText = EditText(this)
      editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD

      AlertDialog.Builder(this)
          .setTitle(R.string.enter_your_password)
          .setView(editText)
          .setCancelable(true)
          .setPositiveButton(android.R.string.ok) { _, _ -> upgrade(editText.text.toString()) }
          .show()
    }
  }

  private fun upgrade(password: String) {
    if (password.isEmpty()) {
      Toast.makeText(this, R.string.error_empty, Toast.LENGTH_SHORT).show()
      upgrade()
      return
    }

    Thread({ doUpgrade(password) }, "upgradeThread").start()
  }

  private fun doUpgrade(password: String) {
    var loader: AlertDialog? = null
    runOnUiThread {
      loader = AlertDialog.Builder(this)
          .setCancelable(false)
          .setTitle(R.string.trying_upgrade)
          .setView(ProgressBar(this).apply { animate() })
          .show()
    }
    try {
      Config.password = password
      Auth.apiKey
      runOnUiThread { Toast.makeText(this, R.string.upgrade_success, Toast.LENGTH_SHORT).show() }
      return
    } catch (e: ChangePasswordException) {
      runOnUiThread {
        when (e.reason) {
          ChangePasswordException.Reason.INVALID_PASSWORD -> {
            Toast.makeText(this, R.string.invalid_password, Toast.LENGTH_SHORT).show()
            upgrade()
          }
          ChangePasswordException.Reason.WRONG_OLD_PASSWORD,
          ChangePasswordException.Reason.INVALID_TOKEN ->
            Toast.makeText(this, R.string.invalid_credentials, Toast.LENGTH_LONG).show()
          else -> {
            Log.e(TAG, "Unknown upgrade error", e)
            Toast.makeText(this, getString(R.string.unknown_error),
                Toast.LENGTH_SHORT).show()
          }
        }
      }
    } catch (e: ConnectionException) {
      runOnUiThread { Toast.makeText(this, R.string.connection_error, Toast.LENGTH_SHORT).show() }
    } catch (e: AuthException) {
      Log.e(TAG, "Error during upgrade", e)
    } finally {
      runOnUiThread { loader!!.dismiss() }
    }

    Config.clearPassword()
  }

  override fun onSearchClick() {
    val intent = Intent(this, SearchActivity::class.java)
    startActivity(intent)
  }

  override fun onSuggestionsClick() {
    val intent = Intent(this, SuggestActivity::class.java)
    startActivity(intent)
  }

  private fun moveEntry(index: Int, entry: QueueEntry) {
    MoveSong(entry, index).defaultAction(this)
  }

  override fun onClick(entry: QueueEntry) {
    if (!Auth.hasPermissionNoRefresh(Permission.MOVE)) {
      return
    }
    AlertDialog.Builder(this)
        .setCancelable(true)
        .setTitle(R.string.move_to_top_confirm)
        .setPositiveButton(android.R.string.yes, { _, _ -> moveEntry(0, entry) })
        .setNegativeButton(android.R.string.cancel, { _, _ -> })
        .show()
  }
}
