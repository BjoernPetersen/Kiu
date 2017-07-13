package com.github.bjoernpetersen.q.ui.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.R;
import com.squareup.picasso.Callback.EmptyCallback;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBindAdapter;
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class QueueEntryDataBinder extends DataBinder<QueueEntryDataBinder.ViewHolder> {

  private final QueueEntryListener listener;
  private final List<QueueEntry> items;

  public QueueEntryDataBinder(DataBindAdapter adapter, QueueEntryListener listener) {
    super(adapter);
    this.listener = listener;
    this.items = new ArrayList<>();
  }

  @Override
  public ViewHolder newViewHolder(ViewGroup parent) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_queue, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void bindViewHolder(final ViewHolder holder, int position) {
    QueueEntry entry = getItem(position);
    holder.entry = entry;
    Song song = entry.getSong();

    final ImageView albumArtView = holder.albumArtView;
    Picasso.with(albumArtView.getContext()).cancelRequest(holder.albumArtView);
    holder.albumArtView.setVisibility(View.GONE);
    Picasso.with(albumArtView.getContext())
        .load(song.getAlbumArtUrl())
        .into(holder.albumArtView, new EmptyCallback() {
          @Override
          public void onSuccess() {
            albumArtView.setVisibility(View.VISIBLE);
          }
        });
    setContent(holder.titleView, song.getTitle());
    setContent(holder.descriptionView, song.getDescription());
    setContent(holder.queuerView, entry.getUserName());

    holder.view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != listener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          listener.onClick(holder.entry);
        }
      }
    });
  }

  private void setContent(TextView view, String content) {
    if (content == null || content.isEmpty()) {
      view.setVisibility(View.GONE);
    } else {
      view.setVisibility(View.VISIBLE);
      view.setText(content);
    }
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  QueueEntry getItem(int position) {
    return items.get(position);
  }

  public List<QueueEntry> getItems() {
    return Collections.unmodifiableList(items);
  }

  public void setItems(List<QueueEntry> items) {
    this.items.clear();
    this.items.addAll(items);
    notifyDataSetChanged();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {

    public final View view;
    public final ImageView albumArtView;
    public final TextView titleView;
    public final TextView descriptionView;
    // TODO public final TextView lengthView;
    public final TextView queuerView;
    public QueueEntry entry;

    public ViewHolder(View view) {
      super(view);
      this.view = view;
      this.albumArtView = (ImageView) view.findViewById(R.id.album_art);
      this.titleView = (TextView) view.findViewById(R.id.song_title);
      this.titleView.setSelected(true);
      this.descriptionView = (TextView) view.findViewById(R.id.song_description);
      this.descriptionView.setSelected(true);
      //TODO   this.lengthView = (TextView) view.findViewById(R.id.song_length);
      this.queuerView = (TextView) view.findViewById(R.id.song_queuer);
    }


    @Override
    public String toString() {
      return super.toString() + " '" + titleView.getText() + "'";
    }
  }

  public interface QueueEntryListener {

    void onClick(QueueEntry entry);
  }
}
