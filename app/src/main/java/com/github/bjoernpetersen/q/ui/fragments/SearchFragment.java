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
import com.github.bjoernpetersen.q.api.UiCallback;
import com.squareup.okhttp.Call;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SearchFragment extends Fragment {

  private static final String TAG = SearchFragment.class.getSimpleName();
  private static final String ARG_PROVIDER_ID = "provider-id";

  private String providerId;
  private String currentQuery;
  private Call currentCall;
  private OnFragmentInteractionListener mListener;

  @SuppressWarnings("unused")
  public static SearchFragment newInstance(String providerId) {
    SearchFragment fragment = new SearchFragment();
    Bundle args = new Bundle();
    args.putString(ARG_PROVIDER_ID, providerId);
    fragment.setArguments(args);
    return fragment;
  }

  public SearchFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (getArguments() != null) {
      providerId = getArguments().getString(ARG_PROVIDER_ID);
    }

    if (providerId == null) {
      throw new IllegalStateException("Missing providerId argument");
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
    updateResults("");
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

  public void updateResults(final String query) {
    if (query.equals(currentQuery)) {
      return;
    }

    if (currentCall != null) {
      currentCall.cancel();
    }

    getChildFragmentManager().beginTransaction()
        .replace(R.id.root, new LoadingFragment())
        .commit();

    try {
      currentCall = Connection.get(getContext()).searchSongAsync(providerId, query,
          new UiCallback<List<Song>>() {
            @Override
            protected void onFailureImpl(ApiException e, int statusCode,
                Map<String, List<String>> responseHeaders) {
              Log.v(TAG, "Error searching for " + query + " on " + providerId, e);
              // TODO show error fragment
            }

            @Override
            protected void onSuccessImpl(List<Song> result, int statusCode,
                Map<String, List<String>> responseHeaders) {
              if (!isDetached()) {
                getChildFragmentManager().beginTransaction()
                    .replace(R.id.root, SongFragment.newInstance(result))
                    .commit();
              }
            }
          });
      currentQuery = query;
    } catch (ApiException e) {
      Log.e(TAG, "Error searching for " + query + " on " + providerId, e);
      // TODO show error fragment
    }
  }

  public String getProviderId() {
    return providerId;
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
}
