package com.github.bjoernpetersen.q.ui.fragments;

import android.support.annotation.Nullable;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.QueueState;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.api.ApiKey;
import com.github.bjoernpetersen.q.api.Auth;
import com.github.bjoernpetersen.q.api.AuthException;
import com.github.bjoernpetersen.q.api.Connection;
import com.github.bjoernpetersen.q.api.Permission;
import com.squareup.picasso.Callback.EmptyCallback;
import com.squareup.picasso.Picasso;
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBindAdapter;
import com.yqritc.recyclerviewmultipleviewtypesadapter.DataBinder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


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
    final QueueEntry entry = getItem(position);
    if (Objects.equals(entry, holder.entry)) {
      return;
    }
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
    setContent(holder.durationView, getDurationString(song.getDuration()));
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

    final PopupMenu menu = new PopupMenu(holder.view.getContext(), holder.contextMenu);
    menu.inflate(R.menu.query_item_menu);
    menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
          case R.id.remove_button:
            new Thread(new Runnable() {
              @Override
              public void run() {
                try {
                  String token = Auth.INSTANCE.getApiKey().getRaw();
                  Song song = entry.getSong();
                  List<QueueEntry> newQueue = Connection.INSTANCE.dequeue(
                      token, song.getId(), song.getProvider().getId()
                  );
                  QueueState.getInstance().set(newQueue);
                } catch (ApiException e) {
                  if (e.getCode() == 403) {
                    Auth.INSTANCE.clear();
                  } else {
                    e.printStackTrace();
                    // TODO show toast
                  }
                } catch (AuthException e) {
                  e.printStackTrace();
                  // TODO show toast
                }
              }
            }).start();
            return true;
          default:
            return false;
        }
      }
    });
    holder.contextMenu.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        MenuItem removeButton = menu.getMenu().findItem(R.id.remove_button);
        Auth auth = Auth.INSTANCE;
        ApiKey apiKey = auth.getApiKeyNoRefresh();
        removeButton.setVisible(apiKey != null && apiKey.getUserName().equals(entry.getUserName())
            || auth.hasPermissionNoRefresh(Permission.SKIP));
        menu.show();
      }
    });
  }

  @Nullable
  private String getDurationString(int durationSeconds) {
    if (durationSeconds == 0) {
      return null;
    }
    int seconds = durationSeconds % 60;
    int minutes = (durationSeconds - seconds) / 60;
    return String.format(Locale.US, "%d:%02d", minutes, seconds);
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
    public final TextView durationView;
    public final TextView queuerView;
    public final ImageButton contextMenu;
    public QueueEntry entry;

    public ViewHolder(View view) {
      super(view);
      this.view = view;
      this.albumArtView = (ImageView) view.findViewById(R.id.album_art);
      this.titleView = (TextView) view.findViewById(R.id.song_title);
      this.titleView.setSelected(true);
      this.descriptionView = (TextView) view.findViewById(R.id.song_description);
      this.descriptionView.setSelected(true);
      this.durationView = (TextView) view.findViewById(R.id.song_duration);
      this.queuerView = (TextView) view.findViewById(R.id.song_queuer);
      this.contextMenu = (ImageButton) view.findViewById(R.id.context_menu);
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
