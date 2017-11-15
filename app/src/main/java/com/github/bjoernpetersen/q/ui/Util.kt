@file:JvmName("Util")

package com.github.bjoernpetersen.q.ui

import android.os.Looper
import android.support.v4.app.Fragment
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference
import java.util.*

private fun isUiThread(): Boolean = Looper.getMainLooper().thread === Thread.currentThread()

fun Fragment.runOnUiThread(runnable: () -> Unit) {
  if (!isUiThread()) this.view?.post(runnable)
  else runnable()
}

fun Int.asDuration(): String? {
  if (this == 0) {
    return null
  }
  val seconds = this % 60
  val minutes = (this - seconds) / 60
  return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

interface ObserverUser {
  var observers: MutableList<WeakReference<Disposable>>

  fun initObservers()
  fun Disposable.store() = observers.add(WeakReference(this))
  fun disposeObservers() {
    observers.mapNotNull { it.get() }.forEach { it.dispose() }
    initObservers()
  }
}