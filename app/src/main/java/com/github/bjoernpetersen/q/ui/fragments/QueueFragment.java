package com.github.bjoernpetersen.q.ui.fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.State;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import com.github.bjoernpetersen.jmusicbot.client.model.QueueEntry;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.QueueState;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.api.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A fragment representing a list of Items.
 * <p />
 * Activities containing this fragment MUST implement the {@link ListFragmentInteractionListener}
 * interface.
 */
public class QueueFragment extends Fragment {

  private static final String TAG = QueueFragment.class.getSimpleName();
  private static final String ITEMS_KEY = QueueFragment.class.getName() + "items";

  private ArrayList<QueueEntry> items;
  private ListFragmentInteractionListener mListener;
  private QueueState.Listener queueListener;
  private ScheduledExecutorService updater;
  private ScheduledFuture<?> updateTask;

  /**
   * Mandatory empty constructor for the fragment manager to instantiate the
   * fragment (e.g. upon screen orientation changes).
   */
  public QueueFragment() {
  }

  @SuppressWarnings("unused")
  public static QueueFragment newInstance() {
    QueueFragment fragment = new QueueFragment();
    Bundle args = new Bundle();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      this.items = savedInstanceState.getParcelableArrayList(ITEMS_KEY);
    }
    if (this.items == null) {
      this.items = new ArrayList<>();
    }

    updater = Executors.newSingleThreadScheduledExecutor();
  }

  @Override
  public void onDestroy() {
    updater.shutdown();
    updater = null;
    super.onDestroy();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_queue_list, container, false);

    // Set the adapter
    if (view instanceof RecyclerView) {
      Context context = view.getContext();
      RecyclerView recyclerView = (RecyclerView) view;
      recyclerView.setAdapter(new QueueRecyclerViewAdapter(items, mListener));
      recyclerView.addItemDecoration(
          new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL)
      );
    } else {
      throw new IllegalStateException();
    }

    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    updateQueue(QueueState.getInstance().get());
    QueueState.getInstance().addListener(queueListener = new QueueState.Listener() {
      @Override
      public void onChange(List<QueueEntry> oldQueue, List<QueueEntry> newQueue) {
        updateQueue(newQueue);
      }
    });
    updateTask = updater.scheduleWithFixedDelay(new UpdateTask(), 0, 2, TimeUnit.SECONDS);
  }

  @Override
  public void onStop() {
    updateTask.cancel(true);
    QueueState.getInstance().removeListener(queueListener);
    queueListener = null;
    super.onStop();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putParcelableArrayList(ITEMS_KEY, items);
    super.onSaveInstanceState(outState);
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof ListFragmentInteractionListener) {
      mListener = (ListFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement ListFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  private void updateQueue(List<QueueEntry> queue) {
    items.clear();
    items.addAll(queue);
    items.add(null);
    RecyclerView recyclerView = (RecyclerView) getView();
    if (recyclerView == null) {
      return;
    }
    recyclerView.getAdapter().notifyDataSetChanged();
  }

  private class UpdateTask implements Runnable {

    @Override
    public void run() {
      try {
        final List<QueueEntry> queue = Connection.get(getContext()).getQueue();
        View view = getView();
        if (view == null) {
          return;
        }
        view.post(new Runnable() {
          @Override
          public void run() {
            QueueState.getInstance().set(queue);
          }
        });
      } catch (ApiException e) {
        Log.v(TAG, "Could not get queue", e);
      } catch (RuntimeException e) {
        Log.wtf(TAG, "FUCK", e);
      }
    }
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p/>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface ListFragmentInteractionListener {

    void onSongClick(Song song);

    void showSearch();

    void showSuggestions();
  }
}
