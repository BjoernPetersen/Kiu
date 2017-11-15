package com.github.bjoernpetersen.q.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.bjoernpetersen.jmusicbot.client.ApiException
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry
import com.github.bjoernpetersen.q.QueueState
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.Config
import com.github.bjoernpetersen.q.api.Connection
import com.github.bjoernpetersen.q.api.HostDiscoverer
import com.github.bjoernpetersen.q.tag
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryAdapter.QueueEntryType
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryAddButtonsDataBinder.QueueEntryAddButtonsListener
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryDataBinder.QueueEntryListener
import com.github.bjoernpetersen.q.ui.runOnUiThread
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

private val TAG = QueueFragment::class.java.simpleName
private val ITEMS_KEY = QueueFragment::class.java.name + ".items"

/**
 * A fragment representing a list of Items.
 *
 * Activities containing this fragment MUST implement
 * the [QueueEntryListener] and [QueueEntryAddButtonsListener] interface.
 */
class QueueFragment : Fragment() {

  private var dataBinder: QueueEntryDataBinder? = null
  private var addButtonsListener: QueueEntryAddButtonsListener? = null
  private var entryListener: QueueEntryListener? = null
  private var updater: ScheduledExecutorService? = null
  private var updateTask: ScheduledFuture<*>? = null
  private var listeners: MutableList<Any>? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    updater = Executors.newSingleThreadScheduledExecutor()
  }

  override fun onDestroy() {
    updater?.shutdown()
    updater = null
    super.onDestroy()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_queue_list, container, false)
        as? RecyclerView ?: throw IllegalStateException()

    // Set the adapter
    // TODO set items
    val error = { throw IllegalStateException("Should have been set in OnAttach!") }
    val adapter = QueueEntryAdapter(entryListener ?: error(), addButtonsListener ?: error())
    view.adapter = adapter
    view.addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
    val items: List<QueueEntry> = savedInstanceState?.getParcelableArrayList(
        ITEMS_KEY) ?: ArrayList()
    dataBinder = adapter.getDataBinder(QueueEntryType.QUEUE_ENTRY)
    dataBinder!!.items = items
    return view
  }

  override fun onDestroyView() {
    super.onDestroyView()
    dataBinder = null
  }

  override fun onStart() {
    super.onStart()
    updateQueue(QueueState.queue)
    val listener = { _: Any, newQueue: List<QueueEntry> -> runOnUiThread { updateQueue(newQueue) } }
    listeners?.add(listener) ?: throw IllegalStateException()
    QueueState.addListener(listener)
    updateTask = updater?.scheduleWithFixedDelay(this::retrieveUpdate, 0, 2, TimeUnit.SECONDS)
  }

  override fun onStop() {
    updateTask?.cancel(true)
    super.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    dataBinder?.apply { outState?.putParcelableArrayList(ITEMS_KEY, ArrayList(items)) }
    super.onSaveInstanceState(outState)
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    listeners = ArrayList()
    entryListener = context as? QueueEntryListener ?:
        throw RuntimeException(context?.toString() + " must implement QueueEntryListener")

    addButtonsListener = context as? QueueEntryAddButtonsListener ?:
        throw RuntimeException(context.toString() + " must implement QueueEntryAddButtonsListener")
  }

  override fun onDetach() {
    super.onDetach()
    entryListener = null
    addButtonsListener = null
    listeners = null
  }

  private fun updateQueue(queue: List<QueueEntry>) {
    dataBinder?.items = queue
  }

  private fun retrieveUpdate() {
    Log.v(TAG, "Retrieving queue...")
    try {
      val queue = Connection.getQueue()
      runOnUiThread { QueueState.queue = queue }
    } catch (e: ApiException) {
      if (e.cause is IOException) {
        // try reconnecting
        Observable.fromCallable(HostDiscoverer())
            .subscribeOn(Schedulers.io())
            .subscribe({
              Log.i(tag(), "Found new host: " + it)
              Config.host = it
            }, { Log.v(tag(), "Could not retrieve new host", it) })
      }
      Log.v(TAG, "Could not get queue", e)
    } catch (e: RuntimeException) {
      Log.wtf(TAG, e)
    }
  }

  companion object {
    @JvmStatic
    fun newInstance(): QueueFragment {
      val fragment = QueueFragment()
      val args = Bundle()
      fragment.arguments = args
      return fragment
    }
  }
}
