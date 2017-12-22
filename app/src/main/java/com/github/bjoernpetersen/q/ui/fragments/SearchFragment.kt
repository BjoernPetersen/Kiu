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
import com.github.bjoernpetersen.q.api.action.DiscoverHost
import com.github.bjoernpetersen.q.tag
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.net.SocketTimeoutException


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SearchFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 */
class SearchFragment : Fragment() {

  var provider: NamedPlugin? = null
    private set
  private var searchObserver: Disposable? = null
  private var lastQuery: String? = null
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
  }

  override fun onDetach() {
    super.onDetach()
    mListener = null
  }

  override fun onStop() {
    searchObserver?.dispose()
    lastQuery = null
    super.onStop()
  }

  fun updateResults(query: String) {
    val trimmedQuery = query.trim()
    if (trimmedQuery == lastQuery) return
    lastQuery = trimmedQuery

    searchObserver?.dispose()

    if (provider == null) throw IllegalStateException()

    childFragmentManager.beginTransaction()
        .replace(R.id.root, LoadingFragment())
        .commit()

    searchObserver = Observable.fromCallable { Connection.searchSong(provider!!.id, trimmedQuery) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError {
          val context: Context? = context
          if (context != null && it.cause is SocketTimeoutException) DiscoverHost(context).defaultAction()
        }
        .subscribe(this::showResults, {
          Log.d(tag(), "Error retrieving search results", it)
        })
  }

  private fun showResults(result: List<Song>) {
    if (!isDetached) childFragmentManager.beginTransaction()
        .replace(R.id.root, SongFragment.newInstance(result, R.menu.search_context_menu))
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

  companion object {
    @JvmStatic
    private val ARG_PROVIDER = SearchFragment::class.java.name + ".provider"

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
