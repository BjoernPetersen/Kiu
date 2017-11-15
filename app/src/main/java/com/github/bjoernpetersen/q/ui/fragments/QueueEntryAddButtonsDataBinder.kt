package com.github.bjoernpetersen.q.ui.fragments

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import com.github.bjoernpetersen.q.R
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBindAdapter
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBinder

class QueueEntryAddButtonsDataBinder(dataBindAdapter: DataBindAdapter,
    private val listener: QueueEntryAddButtonsListener?)
  : DataBinder<QueueEntryAddButtonsDataBinder.ViewHolder>(dataBindAdapter) {

  override fun newViewHolder(parent: ViewGroup): ViewHolder {
    val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.fragment_queue_buttons, parent, false)
    return ViewHolder(view)
  }

  override fun bindViewHolder(holder: ViewHolder, position: Int) {
    holder.searchButton.setOnClickListener {
      listener?.onSearchClick()
    }
    holder.searchButton.setOnLongClickListener {
      Toast.makeText(it.context, it.contentDescription, Toast.LENGTH_SHORT).show()
      true
    }
    holder.suggestButton.setOnClickListener {
      listener?.onSuggestionsClick()
    }
    holder.suggestButton.setOnLongClickListener {
      Toast.makeText(it.context, it.contentDescription, Toast.LENGTH_SHORT).show()
      true
    }
  }

  override fun getItemCount(): Int = 1

  class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val searchButton: ImageButton = view.findViewById(R.id.search_button)
    val suggestButton: ImageButton = view.findViewById(R.id.suggest_button)
  }

  interface QueueEntryAddButtonsListener {
    fun onSearchClick()
    fun onSuggestionsClick()
  }
}
