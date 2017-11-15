package com.github.bjoernpetersen.q.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.ui.fragments.SongFragment.OnListFragmentInteractionListener

private const val ARG_SONG_LIST = "song-list"

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
class SongFragment : Fragment() {

  private var songs: ArrayList<Song>? = null
  private var mListener: OnListFragmentInteractionListener? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    songs = arguments?.getParcelableArrayList(ARG_SONG_LIST) ?: ArrayList()
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_song_list, container, false)

    val songs = this.songs ?: throw IllegalStateException(
        "Songs should have been initialized in onCreate")

    // Set the adapter
    if (view is RecyclerView) {
      view.adapter = SongRecyclerViewAdapter(songs, mListener)
      val decoration = DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL)
      view.addItemDecoration(decoration)
    }
    return view
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is OnListFragmentInteractionListener) mListener = context
    else throw RuntimeException(
        context.toString() + " must implement OnListFragmentInteractionListener")
  }

  override fun onDetach() {
    songs = null
    mListener = null
    super.onDetach()
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
  interface OnListFragmentInteractionListener {

    fun onAdd(song: Song)
    fun showAdd(song: Song): Boolean
  }

  companion object {
    @JvmStatic
    fun newInstance(songs: List<Song>): SongFragment {
      val fragment = SongFragment()
      val args = Bundle()
      args.putParcelableArrayList(ARG_SONG_LIST, ArrayList(songs))
      fragment.arguments = args
      return fragment
    }
  }
}
