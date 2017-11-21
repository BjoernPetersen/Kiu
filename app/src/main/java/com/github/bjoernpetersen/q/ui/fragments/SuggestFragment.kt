package com.github.bjoernpetersen.q.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.bjoernpetersen.jmusicbot.client.model.NamedPlugin
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.Connection
import com.github.bjoernpetersen.q.tag
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [SuggestFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 */
class SuggestFragment : Fragment() {

  var suggester: NamedPlugin? = null
    private set
  private var mListener: OnFragmentInteractionListener? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    suggester = arguments?.getParcelable(ARG_SUGGESTER) ?:
        throw  IllegalStateException("Missing suggesterId argument")
  }

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    // Inflate the layout for this fragment
    return inflater!!.inflate(R.layout.fragment_suggest, container, false)
  }

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    loadSuggestions()
  }

  override fun onAttach(context: Context?) {
    super.onAttach(context)
    if (context is OnFragmentInteractionListener) {
      mListener = context
    } else {
      throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
    }
  }

  override fun onDetach() {
    super.onDetach()
    mListener = null
  }

  fun refresh() {
    loadSuggestions()
  }

  private fun loadSuggestions() {
    childFragmentManager.beginTransaction()
        .replace(R.id.root, LoadingFragment())
        .commit()

    val suggester = suggester!!
    Observable.fromCallable { Connection.suggestSong(suggester.id, null) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({
          childFragmentManager.beginTransaction()
              .replace(R.id.root, SongFragment.newInstance(it, R.menu.suggest_context_menu))
              .commit()
        }, {
          Log.v(tag(), "Could not get suggestions", it)
        })
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
    private val ARG_SUGGESTER = SuggestFragment::class.java.name + ".suggester"

    @JvmStatic
    fun newInstance(suggester: NamedPlugin): SuggestFragment {
      val fragment = SuggestFragment()
      val args = Bundle()
      args.putParcelable(ARG_SUGGESTER, suggester)
      fragment.arguments = args
      return fragment
    }
  }


}
