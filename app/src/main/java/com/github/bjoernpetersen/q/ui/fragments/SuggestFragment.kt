package com.github.bjoernpetersen.q.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.bjoernpetersen.jmusicbot.client.ApiException
import com.github.bjoernpetersen.jmusicbot.client.model.NamedPlugin
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.Connection
import com.github.bjoernpetersen.q.ui.runOnUiThread

private val TAG = SuggestFragment::class.java.simpleName
private const val ARG_SUGGESTER = "suggester"

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
    return inflater!!.inflate(R.layout.fragment_search, container, false)
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

  fun update() {
    loadSuggestions()
  }

  private fun showResults(result: List<Song>) {
    childFragmentManager.beginTransaction()
        .replace(R.id.root, SongFragment.newInstance(result))
        .commit()
  }

  private fun loadSuggestions() {
    childFragmentManager.beginTransaction()
        .replace(R.id.root, LoadingFragment())
        .commit()

    Thread({
      suggester?.let { suggester ->
        try {
          val songs = Connection.suggestSong(suggester.id, null)
          runOnUiThread { showResults(songs) }
        } catch (e: ApiException) {
          Log.v(TAG, "Could not load suggestions", e)
          runOnUiThread {
            if (!isDetached) {
              // TODO show error fragment
            }
          }
        }
      }
    }).start()
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
    fun newInstance(suggester: NamedPlugin): SuggestFragment {
      val fragment = SuggestFragment()
      val args = Bundle()
      args.putParcelable(ARG_SUGGESTER, suggester)
      fragment.arguments = args
      return fragment
    }
  }


}
