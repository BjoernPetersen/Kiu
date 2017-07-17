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
import com.github.bjoernpetersen.jmusicbot.client.model.NamedPlugin;
import com.github.bjoernpetersen.jmusicbot.client.model.Song;
import com.github.bjoernpetersen.q.R;
import com.github.bjoernpetersen.q.api.Connection;
import com.github.bjoernpetersen.q.api.HostDiscoverer;
import java.io.Closeable;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.jar.Attributes.Name;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class SearchFragment extends Fragment {

  private static final String TAG = SearchFragment.class.getSimpleName();
  private static final String ARG_PROVIDER = "provider";

  private NamedPlugin provider;
  private SearchExecutor searchExecutor;
  private OnFragmentInteractionListener mListener;

  @SuppressWarnings("unused")
  public static SearchFragment newInstance(NamedPlugin provider) {
    SearchFragment fragment = new SearchFragment();
    Bundle args = new Bundle();
    args.putParcelable(ARG_PROVIDER, provider);
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
      provider = getArguments().getParcelable(ARG_PROVIDER);
    }

    if (provider == null) {
      throw new IllegalStateException("Missing provider argument");
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
    searchExecutor = new SearchExecutor();
  }

  @Override
  public void onDetach() {
    super.onDetach();
    searchExecutor.close();
    searchExecutor = null;
    mListener = null;
  }

  public void updateResults(final String query) {
    searchExecutor.search(query);
  }

  void showResults(List<Song> result) {
    if (!isDetached()) {
      getChildFragmentManager().beginTransaction()
          .replace(R.id.root, SongFragment.newInstance(result))
          .commit();
    }
  }

  public NamedPlugin getProvider() {
    return provider;
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

  private class SearchExecutor implements Closeable {

    private final ExecutorService executor;
    private final Runnable resultListenerTask;

    private String currentQuery;
    private Future<?> resultListener;
    private Future<List<Song>> searchFuture;

    SearchExecutor() {
      executor = Executors.newFixedThreadPool(2);
      resultListenerTask = new Runnable() {
        @Override
        public void run() {
          try {
            final List<Song> result = searchFuture.get();
            if (!isDetached()) {
              View view = getView();
              if (view == null) {
                Log.w(TAG, "Not detached, but view is null");
                return;
              }
              view.post(new Runnable() {
                @Override
                public void run() {
                  showResults(result);
                }
              });
            }
          } catch (InterruptedException e) {
            Log.v(TAG, "Interrupted while waiting for search results", e);
          } catch (ExecutionException e) {
            Log.d(TAG, "Error retrieving search results", e);
            if(e.getCause() instanceof SocketTimeoutException) {
              executor.submit((Runnable) new HostDiscoverer());
            }
          } catch (CancellationException e) {
            Log.v(TAG, "Search result waiter was cancelled");
          }
        }
      };
    }

    private void search(String query) {
      if (query.equals(currentQuery)) {
        return;
      }

      if (searchFuture != null) {
        resultListener.cancel(true);
        searchFuture.cancel(true);
      }

      getChildFragmentManager().beginTransaction()
          .replace(R.id.root, new LoadingFragment())
          .commit();

      this.currentQuery = query;
      this.searchFuture = enqueue(query);
      this.resultListener = executor.submit(resultListenerTask);
    }

    private Future<List<Song>> enqueue(final String query) {
      return executor.submit(new Callable<List<Song>>() {
        @Override
        public List<Song> call() throws ApiException {
          return Connection.INSTANCE.searchSong(provider.getId(), query);
        }
      });
    }

    @Override
    public void close() {
      executor.shutdown();
    }
  }
}
