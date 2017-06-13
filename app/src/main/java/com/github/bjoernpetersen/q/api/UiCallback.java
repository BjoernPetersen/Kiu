package com.github.bjoernpetersen.q.api;

import android.os.Handler;
import com.github.bjoernpetersen.jmusicbot.client.ApiCallback;
import com.github.bjoernpetersen.jmusicbot.client.ApiException;
import java.util.List;
import java.util.Map;

public abstract class UiCallback<T> implements ApiCallback<T> {

  private final Handler handler;

  public UiCallback() {
    handler = new Handler();
  }


  @Override
  public final void onFailure(final ApiException e, final int statusCode,
      final Map<String, List<String>> responseHeaders) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        onFailureImpl(e, statusCode, responseHeaders);
      }
    });
  }

  protected abstract void onFailureImpl(ApiException e, int statusCode,
      Map<String, List<String>> responseHeaders);

  @Override
  public final void onSuccess(final T result, final int statusCode,
      final Map<String, List<String>> responseHeaders) {
    handler.post(new Runnable() {
      @Override
      public void run() {
        onSuccessImpl(result, statusCode, responseHeaders);
      }
    });
  }

  protected abstract void onSuccessImpl(T result, int statusCode,
      Map<String, List<String>> responseHeaders);

  @Override
  public void onUploadProgress(long bytesWritten, long contentLength, boolean done) {
  }

  @Override
  public void onDownloadProgress(long bytesRead, long contentLength, boolean done) {
  }
}
