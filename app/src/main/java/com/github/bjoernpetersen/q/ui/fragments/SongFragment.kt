package com.github.bjoernpetersen.q.ui.fragments

import android.content.Context
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.annotation.MenuRes
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.ui.fragments.SongFragment.SongFragmentInteractionListener

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [SongFragmentInteractionListener]
 * interface.
 */
class SongFragment : Fragment() {

  private var songs: ArrayList<Song>? = null
  private var songContext: Int? = null
  private var mListener: SongFragmentInteractionListener? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val args = arguments ?: throw IllegalStateException("Arguments are missing")
    songs = args.getParcelableArrayList(ARG_SONG_LIST) ?: ArrayList()
    songContext = if (args.containsKey(ARG_SONG_CONTEXT)) args.getInt(ARG_SONG_CONTEXT) else null
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    val view = inflater.inflate(R.layout.fragment_song_list, container, false)

    val songs = this.songs ?: throw IllegalStateException()

    // Set the adapter
    if (view is RecyclerView) {
      view.adapter = SongRecyclerViewAdapter(songs, mListener, songContext)
      val decoration = DividerItemDecoration(view.context, DividerItemDecoration.VERTICAL)
      view.addItemDecoration(decoration)
    }
    return view
  }

  override fun onAttach(context: Context) {
    super.onAttach(context)
    if (context is SongFragmentInteractionListener) mListener = context
    else throw RuntimeException(
        context.toString() + " must implement SongFragmentInteractionListener")
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
  interface SongFragmentInteractionListener {

    fun onClick(song: Song, enable: EnableCallback)
    fun isEnabled(song: Song): Boolean
    fun isEnabled(song: Song, @IdRes menuItemId: Int): Boolean
    fun onContextMenu(song: Song, menuItem: MenuItem, enable: (Boolean) -> Unit): Boolean
    fun onSongListTouch() = Unit
  }

  companion object {
    @JvmStatic
    private val ARG_SONG_LIST = SongFragment::class.java.name + ".songList"
    @JvmStatic
    private val ARG_SONG_CONTEXT = SongFragment::class.java.name + ".songContext"

    @JvmStatic
    fun newInstance(songs: List<Song>, @MenuRes songContext: Int? = null): SongFragment {
      val fragment = SongFragment()
      val args = Bundle()
      args.putParcelableArrayList(ARG_SONG_LIST, ArrayList(songs))
      if (songContext != null)
        args.putInt(ARG_SONG_CONTEXT, songContext)
      fragment.arguments = args
      return fragment
    }
  }
}
