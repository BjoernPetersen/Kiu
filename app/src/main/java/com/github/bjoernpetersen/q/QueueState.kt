package com.github.bjoernpetersen.q

import android.os.Looper
import android.support.annotation.UiThread
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry
import java.lang.ref.WeakReference
import java.util.*

@UiThread
object QueueState {

  private val listeners: MutableSet<WeakListener> = HashSet()
  var queue: List<QueueEntry> = emptyList()
    @UiThread
    set(value) {
      if (Looper.getMainLooper().thread != Thread.currentThread()) {
        throw RuntimeException("Must be called on UI thread")
      }
      val oldQueue = this.queue
      field = value
      for (listener in listeners) {
        listener(oldQueue, value)
      }
    }

  @UiThread
  fun addListener(listener: (List<QueueEntry>, List<QueueEntry>) -> Unit) {
    listeners.add(WeakListener(listener))
  }

  @UiThread
  fun removeListener(listener: (List<QueueEntry>, List<QueueEntry>) -> Unit) {
    listeners.remove(WeakListener(listener))
  }

  private class WeakListener(listener: (List<QueueEntry>, List<QueueEntry>) -> Unit) :
      (List<QueueEntry>, List<QueueEntry>) -> Unit {

    private val listener: WeakReference<(List<QueueEntry>, List<QueueEntry>) -> Unit> = WeakReference(
        listener)

    override fun invoke(oldQueue: List<QueueEntry>, newQueue: List<QueueEntry>) {
      val listener = this.listener.get()
      listener?.invoke(oldQueue, newQueue) ?: listeners.remove(this)
    }

    override fun equals(other: Any?): Boolean {
      if (this === other) {
        return true
      }
      if (other == null || javaClass != other.javaClass) {
        return false
      }

      other as WeakListener
      return other.listener.get() == this.listener.get()
    }

    override fun hashCode(): Int {
      val listener = this.listener.get()
      return listener?.hashCode() ?: 0
    }
  }
}
