package com.github.bjoernpetersen.q.ui.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.ui.fragments.SongFragment.OnListFragmentInteractionListener;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Song} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class SongRecyclerViewAdapter extends
    RecyclerView.Adapter<SongRecyclerViewAdapter.ViewHolder> {

  private final List<Song> mValues;
  private final OnListFragmentInteractionListener mListener;

  public SongRecyclerViewAdapter(List<Song> items, OnListFragmentInteractionListener listener) {
    mValues = items;
    mListener = listener;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
        .inflate(R.layout.fragment_song, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(final ViewHolder holder, int position) {
    final Song song = holder.song = mValues.get(position);
    Picasso.with(holder.view.getContext())
        .load(song.getAlbumArtUrl())
        .placeholder(R.drawable.ic_broken_image)
        .into(holder.albumArtView);
    setContent(holder.titleView, song.getTitle());
    setContent(holder.descriptionView, song.getDescription());
    holder.addButton.setVisibility(View.VISIBLE);
    if (mListener != null) {
      boolean showAdd = mListener.showAdd(song);
      holder.addButton.setVisibility(showAdd ? View.VISIBLE : View.GONE);
    }

    holder.addButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (null != mListener) {
          // Notify the active callbacks interface (the activity, if the
          // fragment is attached to one) that an item has been selected.
          mListener.onAdd(song);
          holder.addButton.setVisibility(View.GONE);
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
    public final Button addButton;
    public Song song;

    public ViewHolder(View view) {
      super(view);
      this.view = view;
      albumArtView = (ImageView) view.findViewById(R.id.album_art);
      titleView = (TextView) view.findViewById(R.id.song_title);
      titleView.setSelected(true);
      descriptionView = (TextView) view.findViewById(R.id.song_description);
      descriptionView.setSelected(true);
      // TODO lengthView = (TextView) view.findViewById(R.id.song_length);
      addButton = (Button) view.findViewById(R.id.add_button);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + titleView.getText() + "'";
    }
  }
}
