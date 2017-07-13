package com.github.bjoernpetersen.q.ui.fragments;

import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry;
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryAddButtonsDataBinder.QueueEntryAddButtonsListener;
import com.github.bjoernpetersen.q.ui.fragments.QueueEntryDataBinder.QueueEntryListener;
import com.yqritc.recyclerviewmultipleviewtypesadapter.EnumListBindAdapter;
import java.util.Objects;

public class QueueEntryAdapter extends EnumListBindAdapter<QueueEntryAdapter.QueueEntryType> {

  public enum QueueEntryType {
    QUEUE_ENTRY, ADD_BUTTONS
  }

  QueueEntryAdapter(QueueEntryListener entryListener,
      QueueEntryAddButtonsListener buttonsListener) {
    super();
    addAllBinder(
        new QueueEntryDataBinder(this, entryListener),
        new QueueEntryAddButtonsDataBinder(this, buttonsListener)
    );
    setHasStableIds(true);
  }

  @Override
  public long getItemId(int position) {
    int type = getItemViewType(position);
    if (type == QueueEntryType.ADD_BUTTONS.ordinal()) {
      return 0;
    }

    QueueEntry entry = getItem(position);
    return Objects.hash(entry.getSong(), entry.getUserName());
  }

  public QueueEntry getItem(int position) {
    QueueEntryDataBinder dataBinder = getDataBinder(QueueEntryType.QUEUE_ENTRY.ordinal());
    return dataBinder.getItem(position);
  }
}
