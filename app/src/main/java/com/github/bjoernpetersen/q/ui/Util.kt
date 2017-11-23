@file:JvmName("Util")

package com.github.bjoernpetersen.q.ui

import android.view.View
import io.reactivex.disposables.Disposable
import java.lang.ref.WeakReference
import java.util.*

var View.isVisible: Boolean
  get() = visibility == View.VISIBLE
  set(value) {
    visibility = if (value) View.VISIBLE else View.GONE
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