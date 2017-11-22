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
import com.github.bjoernpetersen.q.api.Connection
import com.github.bjoernpetersen.q.api.action.DiscoverHost
import com.github.bjoernpetersen.q.tag
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryAdapter.QueueEntryType
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryAddButtonsDataBinder.QueueEntryAddButtonsListener
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryDataBinder.QueueEntryListener
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.util.concurrent.TimeUnit

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
  private var updater: Disposable? = null
  private var listener: ((List<QueueEntry>, List<QueueEntry>) -> Unit)? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_queue_list, container, false)
        as? RecyclerView ?: throw IllegalStateException()

    val error = { throw IllegalStateException("Should have been set in onAttach!") }
    val adapter = QueueEntryAdapter(entryListener ?: error(), addButtonsListener ?: error())
    view.adapter = adapter
    view.addItemDecoration(DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL))
    val items: List<QueueEntry> =
        savedInstanceState?.getParcelableArrayList(ITEMS_KEY) ?: ArrayList()
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

    listener = { _, newQueue: List<QueueEntry> -> updateQueue(newQueue) }
    QueueState.addListener(listener!!)

    updater = startUpdater()
  }

  override fun onStop() {
    updater?.dispose()
    updater = null
    QueueState.removeListener(listener!!)
    listener = null
    super.onStop()
  }

  override fun onSaveInstanceState(outState: Bundle?) {
    dataBinder?.apply { outState?.putParcelableArrayList(ITEMS_KEY, ArrayList(items)) }
    super.onSaveInstanceState(outState)
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    entryListener = context as? QueueEntryListener ?:
        throw RuntimeException(context?.toString() + " must implement QueueEntryListener")

    addButtonsListener = context as? QueueEntryAddButtonsListener ?:
        throw RuntimeException(context.toString() + " must implement QueueEntryAddButtonsListener")
  }

  override fun onDetach() {
    super.onDetach()
    entryListener = null
    addButtonsListener = null
  }

  private fun updateQueue(queue: List<QueueEntry>) {
    dataBinder?.items = queue
  }

  private fun startUpdater() = Observable.interval(2, TimeUnit.SECONDS)
      .subscribeOn(Schedulers.io())
      .map { Connection.getQueue() }
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({
        QueueState.queue = it
      }, {
        Log.d(tag(), "Could not retrieve Queue.")
        when (it) {
          is ApiException -> if (it.cause is IOException) {
            DiscoverHost().defaultAction()
          }
          is RuntimeException -> Log.wtf(tag(), it)
        }
      })

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
