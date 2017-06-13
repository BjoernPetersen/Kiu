package com.github.bjoernpetersen.q.ui.fragments;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.ui.fragments.QueueFragment.ListFragmentInteractionListener;
import com.squareup.picasso.Picasso;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link QueueEntry} and makes a call to the
 * specified {@link ListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class QueueRecyclerViewAdapter extends
    RecyclerView.Adapter<QueueRecyclerViewAdapter.ViewHolder> {

  private final List<QueueEntry> mValues;
  private final ListFragmentInteractionListener mListener;

  public QueueRecyclerViewAdapter(List<QueueEntry> items,
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
    QueueEntry entry = holder.entry = mValues.get(position);
    if (entry == null) {
      showActual(holder, false);
      holder.searchButton.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          if (null != mListener) {
            mListener.showSearch();
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
          if (null != mListener) {
            mListener.showSuggestions();
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
    } else {
      showActual(holder, true);
      Song song = entry.getSong();
      Picasso.with(holder.view.getContext())
          .load(song.getAlbumArtUrl())
          .placeholder(R.drawable.ic_broken_image)
          .into(holder.albumArtView);
      setContent(holder.titleView, song.getTitle());
      setContent(holder.descriptionView, song.getDescription());
      setContent(holder.queuerView, entry.getUserName());

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
  }

  private void showActual(ViewHolder holder, boolean show) {
    holder.actualView.setVisibility(show ? View.VISIBLE : View.GONE);
    holder.addView.setVisibility(show ? View.GONE : View.VISIBLE);
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
    public final View actualView;
    public final View addView;

    public final ImageView albumArtView;
    public final TextView titleView;
    public final TextView descriptionView;
    // TODO public final TextView lengthView;
    public final TextView queuerView;
    public QueueEntry entry;

    public final ImageButton searchButton;
    public final ImageButton suggestButton;

    public ViewHolder(View view) {
      super(view);
      this.view = view;
      this.actualView = view.findViewById(R.id.actual_entry);
      this.addView = view.findViewById(R.id.add_buttons);

      albumArtView = (ImageView) view.findViewById(R.id.album_art);
      titleView = (TextView) view.findViewById(R.id.song_title);
      titleView.setSelected(true);
      descriptionView = (TextView) view.findViewById(R.id.song_description);
      descriptionView.setSelected(true);
      //TODO   lengthView = (TextView) view.findViewById(R.id.song_length);
      queuerView = (TextView) view.findViewById(R.id.song_queuer);

      searchButton = (ImageButton) view.findViewById(R.id.search_button);
      suggestButton = (ImageButton) view.findViewById(R.id.suggest_button);
    }

    @Override
    public String toString() {
      return super.toString() + " '" + titleView.getText() + "'";
    }
  }
}
