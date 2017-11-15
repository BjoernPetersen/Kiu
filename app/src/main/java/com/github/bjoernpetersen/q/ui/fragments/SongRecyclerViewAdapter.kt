package com.github.bjoernpetersen.q.ui.fragments

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.ui.asDuration
import com.github.bjoernpetersen.q.ui.fragments.SongFragment.OnListFragmentInteractionListener
import com.squareup.picasso.Callback.EmptyCallback
import com.squareup.picasso.Picasso

/**
 * [RecyclerView.Adapter] that can display a [Song] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class SongRecyclerViewAdapter(private val mValues: List<Song>,
    private val mListener: OnListFragmentInteractionListener?) :
    RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.fragment_song, parent, false)
    return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    holder.song = mValues[position]
    val song = holder.song!!
    Picasso.with(holder.context).cancelRequest(holder.albumArtView)
    holder.albumArtView.visibility = View.GONE
    Picasso.with(holder.context)
        .load(song.albumArtUrl)
        .into(holder.albumArtView, object : EmptyCallback() {
          override fun onSuccess() {
            holder.albumArtView.visibility = View.VISIBLE
          }
        })
    setContent(holder.titleView, song.title)
    setContent(holder.descriptionView, song.description)
    setContent(holder.durationView, song.duration.asDuration())
    holder.addButton.visibility = View.VISIBLE
    if (mListener != null) {
      val showAdd = mListener.showAdd(song)
      holder.addButton.visibility = if (showAdd) View.VISIBLE else View.GONE
    }

    holder.addButton.setOnClickListener {
      if (null != mListener) {
        // Notify the active callbacks interface (the activity, if the
        // fragment is attached to one) that an item has been selected.
        mListener.onAdd(song)
        holder.addButton.visibility = View.GONE
      }
    }
  }

  private fun setContent(view: TextView, content: String?) {
    if (content == null || content.isBlank()) {
      view.visibility = View.GONE
    } else {
      view.visibility = View.VISIBLE
      view.text = content
    }
  }

  override fun getItemCount(): Int = mValues.size

  class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val albumArtView: ImageView = view.findViewById(R.id.album_art)
    val titleView: TextView = view.findViewById(R.id.song_title)
    val descriptionView: TextView = view.findViewById(R.id.song_description)
    val durationView: TextView = view.findViewById(R.id.song_duration)
    val addButton: Button = view.findViewById(R.id.add_button)
    var song: Song? = null
    val context: Context
      get() = view.context

    init {
      descriptionView.isSelected = true
      titleView.isSelected = true
    }

    override fun toString(): String = "${super.toString()} '${titleView.text}'"
  }
}
