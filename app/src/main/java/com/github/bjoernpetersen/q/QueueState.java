package com.github.bjoernpetersen.q;

import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class QueueState {

  private static final String TAG = QueueState.class.getSimpleName();
  private static final QueueState INSTANCE = new QueueState();

  private final Set<WeakListener> listeners;
  private List<QueueEntry> queue;

  private QueueState() {
    this.listeners = new HashSet<>();
    this.queue = Collections.emptyList();
  }

  public static QueueState getInstance() {
    return INSTANCE;
  }

  public List<QueueEntry> get() {
    return queue;
  }

  public void set(List<QueueEntry> queue) {
    if (queue == null) {
      throw new NullPointerException();
    }
    List<QueueEntry> oldQueue = this.queue;
    this.queue = Collections.unmodifiableList(queue);
    for (WeakListener listener : listeners) {
      listener.onChange(oldQueue, this.queue);
    }
  }

  public void addListener(Listener listener) {
    listeners.add(new WeakListener(listener));
  }

  public void removeListener(Listener listener) {
    listeners.remove(new WeakListener(listener));
  }

  private final class WeakListener implements Listener {

    private final WeakReference<Listener> listener;

    WeakListener(Listener listener) {
      this.listener = new WeakReference<>(listener);
    }

    @Override
    public void onChange(List<QueueEntry> oldQueue, List<QueueEntry> newQueue) {
      Listener listener = this.listener.get();
      if (listener == null) {
        listeners.remove(this);
      } else {
        listener.onChange(oldQueue, newQueue);
      }
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      WeakListener other = (WeakListener) o;
      Listener otherListener = other.listener.get();
      Listener listener = this.listener.get();
      if (listener == null) {
        return otherListener == null;
      }
      return listener.equals(otherListener);
    }

    @Override
    public int hashCode() {
      Listener listener = this.listener.get();
      return listener == null ? 0 : listener.hashCode();
    }
  }

  public interface Listener {

    void onChange(List<QueueEntry> oldQueue, List<QueueEntry> newQueue);
  }
}
