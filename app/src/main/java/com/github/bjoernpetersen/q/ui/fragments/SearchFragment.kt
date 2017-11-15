package com.github.bjoernpetersen.q.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.bjoernpetersen.jmusicbot.client.model.NamedPlugin
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.Connection
import com.github.bjoernpetersen.q.api.HostDiscoverer
import java.io.Closeable
import java.net.SocketTimeoutException
import java.util.concurrent.*

private val TAG = SearchFragment::class.java.simpleName
private val ARG_PROVIDER = "provider"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 */
class SearchFragment : Fragment() {

  var provider: NamedPlugin? = null
    private set
  private var searchExecutor: SearchExecutor? = null
  private var mListener: OnFragmentInteractionListener? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    provider = arguments?.getParcelable(ARG_PROVIDER)
        ?: throw IllegalStateException("Missing provider argument")
  }

  override fun onCreateView(inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?): View? =
      inflater.inflate(R.layout.fragment_search, container, false)

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    updateResults("")
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    mListener = if (context is OnFragmentInteractionListener) context
    else throw RuntimeException(
        context.toString() + " must implement OnFragmentInteractionListener")
    searchExecutor = SearchExecutor()
  }

  override fun onDetach() {
    super.onDetach()
    searchExecutor?.close()
    searchExecutor = null
    mListener = null
  }

  fun updateResults(query: String) {
    searchExecutor?.search(query)
  }

  internal fun showResults(result: List<Song>) {
    if (!isDetached) childFragmentManager.beginTransaction()
        .replace(R.id.root, SongFragment.newInstance(result))
        .commit()
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   *
   *
   * See the Android Training lesson [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html) for more information.
   */
  interface OnFragmentInteractionListener

  private inner class SearchExecutor internal constructor() : Closeable {

    private val executor: ExecutorService = Executors.newFixedThreadPool(2)
    private val resultListenerTask: () -> Unit = this::onResult

    private var currentQuery: String? = null
    private var resultListener: Future<*>? = null
    private var searchFuture: Future<List<Song>>? = null

    private fun onResult() {
      try {
        val result = searchFuture!!.get()
        if (!isDetached) {
          val view = view
          if (view == null) {
            Log.w(TAG, "Not detached, but view is null")
            return
          }
          view.post { showResults(result) }
        }
      } catch (e: InterruptedException) {
        Log.v(TAG, "Interrupted while waiting for search results", e)
      } catch (e: ExecutionException) {
        Log.d(TAG, "Error retrieving search results", e)
        if (e.cause is SocketTimeoutException) {
          executor.submit(HostDiscoverer())
        }
      } catch (e: CancellationException) {
        Log.v(TAG, "Search result waiter was cancelled")
      }
    }

    internal fun search(query: String) {
      if (query == currentQuery) {
        return
      }

      resultListener?.cancel(true)
      searchFuture?.cancel(true)

      childFragmentManager.beginTransaction()
          .replace(R.id.root, LoadingFragment())
          .commit()

      this.currentQuery = query
      this.searchFuture = enqueue(query)
      this.resultListener = executor.submit(resultListenerTask)
    }

    private fun enqueue(query: String): Future<List<Song>> =
        executor.submit(Callable { Connection.searchSong(provider!!.id, query) })

    override fun close() = executor.shutdown()
  }

  companion object {
    @JvmStatic
    fun newInstance(provider: NamedPlugin): SearchFragment {
      val fragment = SearchFragment()
      val args = Bundle()
      args.putParcelable(ARG_PROVIDER, provider)
      fragment.arguments = args
      return fragment
    }
  }
}
