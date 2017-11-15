package com.github.bjoernpetersen.q.ui.fragments

import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryAddButtonsDataBinder.QueueEntryAddButtonsListener
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryDataBinder.QueueEntryListener
import com.yqritc.recyclerviewmultipleviewtypesadapter.EnumListBindAdapter
import java.util.*

class QueueEntryAdapter(entryListener: QueueEntryListener,
    buttonsListener: QueueEntryAddButtonsListener)
  : EnumListBindAdapter<QueueEntryAdapter.QueueEntryType>() {

  enum class QueueEntryType {
    QUEUE_ENTRY, ADD_BUTTONS
  }

  init {
    addAllBinder(
        QueueEntryDataBinder(this, entryListener),
        QueueEntryAddButtonsDataBinder(this, buttonsListener)
    )
    setHasStableIds(true)
  }

  override fun getItemId(position: Int): Long {
    val type = getItemViewType(position)
    if (type == QueueEntryType.ADD_BUTTONS.ordinal) {
      return 0
    }

    val entry = getItem(position)
    return Objects.hash(entry.song, entry.userName).toLong()
  }

  private fun getItem(position: Int): QueueEntry {
    val dataBinder = getDataBinder<QueueEntryDataBinder>(QueueEntryType.QUEUE_ENTRY.ordinal)
    return dataBinder.getItem(position)
  }
}
