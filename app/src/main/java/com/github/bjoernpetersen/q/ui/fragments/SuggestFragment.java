package com.github.bjoernpetersen.q.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.api.Connection;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SuggestFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SuggestFragment extends Fragment {

  private static final String TAG = SuggestFragment.class.getSimpleName();
  private static final String ARG_SUGGESTER_ID = "suggester-id";

  private String suggesterId;
  private OnFragmentInteractionListener mListener;

  @SuppressWarnings("unused")
  public static SuggestFragment newInstance(String suggesterId) {
    SuggestFragment fragment = new SuggestFragment();
    Bundle args = new Bundle();
    args.putString(ARG_SUGGESTER_ID, suggesterId);
    fragment.setArguments(args);
    return fragment;
  }

  public SuggestFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      suggesterId = getArguments().getString(ARG_SUGGESTER_ID);
    }

    if (suggesterId == null) {
      throw new IllegalStateException("Missing suggesterId argument");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_search, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    loadSuggestions();
  }

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
    if (context instanceof OnFragmentInteractionListener) {
      mListener = (OnFragmentInteractionListener) context;
    } else {
      throw new RuntimeException(context.toString()
          + " must implement OnFragmentInteractionListener");
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mListener = null;
  }

  public void update() {
    loadSuggestions();
  }

  private void showResults(List<Song> result) {
    getChildFragmentManager().beginTransaction()
        .replace(R.id.root, SongFragment.newInstance(result))
        .commit();
  }

  public String getSuggesterId() {
    return suggesterId;
  }

  /**
   * This interface must be implemented by activities that contain this
   * fragment to allow an interaction in this fragment to be communicated
   * to the activity and potentially other fragments contained in that
   * activity.
   * <p>
   * See the Android Training lesson <a href=
   * "http://developer.android.com/training/basics/fragments/communicating.html"
   * >Communicating with Other Fragments</a> for more information.
   */
  public interface OnFragmentInteractionListener {

  }

  private void runOnUiThread(Runnable runnable) {
    View view = getView();
    if (view != null) {
      view.post(runnable);
    }
  }

  private void loadSuggestions() {
    getChildFragmentManager().beginTransaction()
        .replace(R.id.root, new LoadingFragment())
        .commit();

    new Thread(new Runnable() {
      List<Song> songs;

      @Override
      public void run() {
        try {
          songs = Connection.get(getContext()).suggestSong(suggesterId, null);
        } catch (ApiException e) {
          Log.v(TAG, "Could not load suggestions", e);
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              if (!isDetached()) {
                // TODO show error fragment
              }
            }
          });
        }

        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            showResults(songs);
          }
        });
      }
    }).start();

  }
}
