package com.github.bjoernpetersen.q.ui.fragments

import android.content.Context
import android.support.annotation.MenuRes
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import com.github.bjoernpetersen.jmusicbot.client.model.Song
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.ui.asDuration
import com.github.bjoernpetersen.q.ui.fragments.SongFragment.SongFragmentInteractionListener
import com.github.bjoernpetersen.q.ui.isVisible
import com.squareup.picasso.Callback.EmptyCallback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_song.view.*

typealias EnableCallback = (Boolean) -> Unit

/**
 * [RecyclerView.Adapter] that can display a [Song] and makes a call to the
 * specified [SongFragmentInteractionListener].
 */
class SongRecyclerViewAdapter(private val mValues: List<Song>,
    private val mListener: SongFragmentInteractionListener?,
    @MenuRes private val contextRes: Int?) :
    RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.fragment_song, parent, false)
    return ViewHolder(view)
  }

  private var View.isActive: Boolean
    get() = isEnabled
    set(value) {
      isEnabled = value
      alpha = if (value) 1.0f else 0.5f
    }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    val song = mValues[position]
    holder.song = song
    Picasso.with(holder.context).cancelRequest(holder.albumArtView)
    holder.albumArtView.isVisible = false
    Picasso.with(holder.context)
        .load(song.albumArtUrl)
        .into(holder.albumArtView, object : EmptyCallback() {
          override fun onSuccess() {
            holder.albumArtView.isVisible = true
          }
        })
    holder.titleView.setContent(song.title)
    holder.descriptionView.setContent(song.description)
    holder.durationView.setContent(song.duration.asDuration())
    val view = holder.view
    val contextButton = holder.contextButton
    view.setOnClickListener {
      if (mListener?.isEnabled(song) == true)
        mListener.onClick(song, { view.isActive = it })
    }

    if (contextRes == null) {
      contextButton.isVisible = false
      contextButton.setOnClickListener { }
    } else {
      contextButton.isVisible = true
      val menu = PopupMenu(holder.context, contextButton)
      menu.inflate(contextRes)
      menu.setOnMenuItemClickListener { item ->
        mListener?.onContextMenu(song, item, { view.isActive = it }) ?: true
      }
      contextButton.setOnClickListener {
        val items = menu.menu
        for (itemIndex in 0 until items.size()) {
          val item = items.getItem(itemIndex)
          val isEnabled = mListener?.isEnabled(song, item.itemId) == true
          item.isVisible = isEnabled
        }
        menu.show()
      }
    }

    val isEnabled = mListener?.isEnabled(song) ?: true
    view.isActive = isEnabled
  }

  private fun TextView.setContent(content: String?) {
    if (content == null || content.isBlank()) {
      visibility = View.GONE
    } else {
      visibility = View.VISIBLE
      text = content
    }
  }

  override fun getItemCount(): Int = mValues.size

  class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    val albumArtView: ImageView = view.album_art
    val titleView: TextView = view.song_title
    val descriptionView: TextView = view.song_description
    val durationView: TextView = view.song_duration
    val contextButton: ImageButton = view.context_menu
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
