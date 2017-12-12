package com.github.bjoernpetersen.q.api.action

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.github.bjoernpetersen.jmusicbot.client.ApiException
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry
import com.github.bjoernpetersen.q.QueueState
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.Auth
import com.github.bjoernpetersen.q.api.AuthException
import com.github.bjoernpetersen.q.api.Connection
import com.github.bjoernpetersen.q.tag
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable

class MoveSong(val queueEntry: QueueEntry, val index: Int = 0) : Callable<List<QueueEntry>> {
  @Throws(AuthException::class, ApiException::class)
  override fun call(): List<QueueEntry> {
    val token: String = Auth.apiKey.raw
    return Connection.moveEntry(token, index, queueEntry)
  }

  fun defaultAction(context: Context): Disposable = toSingle()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({
        QueueState.queue = it
        Log.v(tag(), "Successfully moved a song.")
      }, {
        when (it) {
          is AuthException -> Log.d(tag(), "Could not get token", it)
          is ApiException -> Log.d(tag(), "Could not move entry (code: ${it.code}", it)
          else -> Log.i(tag(), "Error moving entry", it)
        }
        Toast.makeText(context, R.string.move_error, Toast.LENGTH_SHORT).show()
      })
}
