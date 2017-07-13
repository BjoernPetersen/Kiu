package com.github.bjoernpetersen.q.ui.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import com.github.bjoernpetersen.q.R;
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBindAdapter;
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBinder;

public class QueueEntryAddButtonsDataBinder extends
    DataBinder<QueueEntryAddButtonsDataBinder.ViewHolder> {

  private final QueueEntryAddButtonsListener listener;

  public QueueEntryAddButtonsDataBinder(DataBindAdapter dataBindAdapter,
      QueueEntryAddButtonsListener listener) {
    super(dataBindAdapter);
    this.listener = listener;
  }

  @Override
  public ViewHolder newViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_queue_buttons, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void bindViewHolder(ViewHolder holder, int position) {
    holder.searchButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != listener) {
          listener.onSearchClick();
        }
      }
    });
    holder.searchButton.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(v.getContext(), v.getContentDescription(), Toast.LENGTH_SHORT).show();
        return true;
      }
    });
    holder.suggestButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != listener) {
          listener.onSuggestionsClick();
        }
      }
    });
    holder.suggestButton.setOnLongClickListener(new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(v.getContext(), v.getContentDescription(), Toast.LENGTH_SHORT).show();
        return true;
      }
    });
  }

  @Override
  public int getItemCount() {
    return 1;
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    public final ImageButton searchButton;
    public final ImageButton suggestButton;

    public ViewHolder(View view) {
      super(view);
      searchButton = (ImageButton) view.findViewById(R.id.search_button);
      suggestButton = (ImageButton) view.findViewById(R.id.suggest_button);
    }
  }

  public interface QueueEntryAddButtonsListener {

    void onSearchClick();

    void onSuggestionsClick();
  }
}
