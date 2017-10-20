package com.github.bjoernpetersen.q.ui.fragments

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.github.bjoernpetersen.jmusicbot.client.ApiException
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry
import com.github.bjoernpetersen.q.QueueState
import com.github.bjoernpetersen.q.R
import com.github.bjoernpetersen.q.api.Auth
import com.github.bjoernpetersen.q.api.AuthException
import com.github.bjoernpetersen.q.api.Connection
import com.github.bjoernpetersen.q.api.Permission
import com.github.bjoernpetersen.q.ui.asDuration
import com.squareup.picasso.Callback.EmptyCallback
import com.squareup.picasso.Picasso
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBindAdapter
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBinder
import java.util.*


class QueueEntryDataBinder(adapter: DataBindAdapter, private val listener: QueueEntryListener?) : DataBinder<QueueEntryDataBinder.ViewHolder>(adapter) {
    private var _items: MutableList<QueueEntry> = ArrayList()
    var items: List<QueueEntry>
        get() = Collections.unmodifiableList(_items)
        set(value) {
            val items = _items
            items.clear()
            items.addAll(value)
            notifyDataSetChanged()
        }

    override fun newViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_queue, parent, false)
        return ViewHolder(view)
    }

    override fun bindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        if (entry == holder.entry) {
            return
        }
        holder.entry = entry
        val song = entry.song

        val albumArtView = holder.albumArtView
        Picasso.with(holder.context).cancelRequest(holder.albumArtView)
        holder.albumArtView.visibility = View.GONE
        Picasso.with(albumArtView.context)
                .load(song.albumArtUrl)
                .into(holder.albumArtView, object : EmptyCallback() {
                    override fun onSuccess() {
                        albumArtView.visibility = View.VISIBLE
                    }
                })
        setContent(holder.titleView, song.title)
        setContent(holder.descriptionView, song.description)
        setContent(holder.durationView, song.duration.asDuration())
        setContent(holder.queuerView, entry.userName)

        holder.view.setOnClickListener {
            holder.entry?.let { listener?.onClick(it) }
        }

        val menu = PopupMenu(holder.view.context, holder.contextMenu)
        menu.inflate(R.menu.query_item_menu)
        menu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.remove_button -> {
                    Thread({
                        try {
                            val token = Auth.apiKey.raw
                            val newQueue = Connection.dequeue(
                                    token, song.id, song.provider.id
                            )
                            QueueState.queue = newQueue
                        } catch (e: ApiException) {
                            if (e.code == 403) {
                                Auth.clear()
                            } else {
                                e.printStackTrace()
                                // TODO show toast
                            }
                        } catch (e: AuthException) {
                            e.printStackTrace()
                            // TODO show toast
                        }
                    }).start()
                    true
                }
                else -> false
            }
        }
        holder.contextMenu.setOnClickListener {
            val removeButton = menu.menu.findItem(R.id.remove_button)
            val auth = Auth
            val apiKey = auth.apiKeyNoRefresh
            removeButton.isVisible = apiKey != null && apiKey.userName == entry.userName || auth.hasPermissionNoRefresh(Permission.SKIP)
            menu.show()
        }
    }

    private fun setContent(view: TextView, content: String?) {
        if (content == null || content.isEmpty()) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
            view.text = content
        }
    }

    override fun getItemCount(): Int = items.size

    internal fun getItem(position: Int): QueueEntry = items[position]


    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val albumArtView: ImageView = view.findViewById(R.id.album_art) as ImageView
        val titleView: TextView = view.findViewById(R.id.song_title) as TextView
        val descriptionView: TextView = view.findViewById(R.id.song_description) as TextView
        val durationView: TextView = view.findViewById(R.id.song_duration) as TextView
        val queuerView: TextView = view.findViewById(R.id.song_queuer) as TextView
        val contextMenu: ImageButton = view.findViewById(R.id.context_menu) as ImageButton
        var entry: QueueEntry? = null
        val context: Context
            get() = view.context

        init {
            this.descriptionView.isSelected = true
            this.titleView.isSelected = true
        }

        override fun toString(): String = "${super.toString()} '${titleView.text}'"
    }

    interface QueueEntryListener {
        fun onClick(entry: QueueEntry)
    }
}
