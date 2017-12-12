package com.github.bjoernpetersen.q.api.action

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.github.bjoernpetersen.jmusicbot.client.ApiException
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import com.github.bjoernpetersen.q.QueueState
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.Auth
import com.github.bjoernpetersen.q.api.Connection
import com.github.bjoernpetersen.q.api.LoginException
import com.github.bjoernpetersen.q.api.RegisterException
import com.github.bjoernpetersen.q.tag
import com.github.bjoernpetersen.q.ui.LoginActivity
import com.github.bjoernpetersen.q.ui.fragments.EnableCallback
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable

class Enqueue(private val song: Song) : Callable<List<QueueEntry>> {

  override fun call(): List<QueueEntry> =
      Connection.enqueue(Auth.apiKey.raw, song.id, song.provider.id)

  fun defaultAction(context: Context, enable: EnableCallback): Disposable {
    enable(false)
    return toSingle()
        .subscribeOn(Schedulers.io())
        .retry({ times, e ->
          if (times < 2 && e is ApiException && e.code == 401) {
            Auth.clear()
            true
          } else false
        })
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          QueueState.queue = it
          Log.d(tag(), "Successfully added song to queue: ${song.title}")
        }, {
          Log.d(tag(), "Could not add a song.")
          Toast.makeText(context, R.string.enqueue_error, Toast.LENGTH_SHORT).show()
          enable(true)
          when (it) {
            is RegisterException -> if (it.reason == RegisterException.Reason.TAKEN) {
              Toast.makeText(
                  context,
                  R.string.error_username_taken, //TODO i18n
                  Toast.LENGTH_SHORT
              ).show()
              context.startActivity(Intent(context, LoginActivity::class.java))
            }
            is LoginException -> if (it.reason == LoginException.Reason.WRONG_UUID
                || it.reason == LoginException.Reason.WRONG_PASSWORD
                || it.reason == LoginException.Reason.NEEDS_AUTH) {
              Toast.makeText(
                  context,
                  R.string.login_error, //TODO i18n
                  Toast.LENGTH_SHORT
              ).show()
              context.startActivity(Intent(context, LoginActivity::class.java))
            }
          }
        })
  }
}
