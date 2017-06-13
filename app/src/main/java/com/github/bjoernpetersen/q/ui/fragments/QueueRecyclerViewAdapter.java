package com.github.bjoernpetersen.q.ui.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.jmusicbot.client.model.SongEntry;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.ui.fragments.QueueFragment.ListFragmentInteractionListener;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link SongEntry} and makes a call to the
 * specified {@link ListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class QueueRecyclerViewAdapter extends
    RecyclerView.Adapter<QueueRecyclerViewAdapter.ViewHolder> {

  private final List<SongEntry> mValues;
  private final ListFragmentInteractionListener mListener;

  public QueueRecyclerViewAdapter(List<SongEntry> items,
      ListFragmentInteractionListener listener) {
    mValues = items;
    mListener = listener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_queue, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    SongEntry entry = holder.entry = mValues.get(position);
    Song song = entry.getSong();
    Picasso.with(holder.view.getContext())
        .load(song.getAlbumArtUrl())
        .placeholder(R.drawable.ic_broken_image)
        .into(holder.albumArtView);
    setContent(holder.titleView, song.getTitle());
    setContent(holder.descriptionView, song.getDescription());
    String userName = entry.getUserName();
    if (userName == null || userName.isEmpty()) {
      userName = holder.view.getResources().getString(R.string.suggested);
    }
    setContent(holder.queuerView, userName);

    holder.view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onSongClick(holder.entry.getSong());
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
    return mValues.size();
  }

  public class ViewHolder extends RecyclerView.ViewHolder {

    public final View view;
    public final ImageView albumArtView;
    public final TextView titleView;
    public final TextView descriptionView;
    // TODO public final TextView lengthView;
    public final TextView queuerView;
    public SongEntry entry;

    public ViewHolder(View view) {
      super(view);
      this.view = view;
      albumArtView = (ImageView) view.findViewById(R.id.album_art);
      titleView = (TextView) view.findViewById(R.id.song_title);
      descriptionView = (TextView) view.findViewById(R.id.song_description);
      //TODO   lengthView = (TextView) view.findViewById(R.id.song_length);
      queuerView = (TextView) view.findViewById(R.id.song_queuer);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + titleView.getText() + "'";
    }
  }
}
