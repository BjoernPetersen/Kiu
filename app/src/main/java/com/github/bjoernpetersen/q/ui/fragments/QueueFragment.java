package com.github.bjoernpetersen.q.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.jmusicbot.client.model.SongEntry;
import com.github.bjoernpetersen.q.R;
import java.util.ArrayList;
import java.util.List;

/**
 * A fragment representing a list of Items.
 * <p />
 * Activities containing this fragment MUST implement the {@link ListFragmentInteractionListener}
 * interface.
 */
public class QueueFragment extends Fragment {

  private static final String ITEMS_KEY = QueueFragment.class.getName() + "items";

  private ArrayList<SongEntry> items;
  private ListFragmentInteractionListener mListener;

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
    }
    return view;
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

    void onSearchClick();

    void onSuggestClick();

    void onSongClick(Song song);
  }
}
