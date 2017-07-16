package com.github.bjoernpetersen.q.ui.fragments;

import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Toast;

final class Util {

  private Util() {
  }

  static void runOnUiThread(Fragment fragment, Runnable runnable) {
    View view = fragment.getView();
    if (view != null) {
      view.post(runnable);
    }
  }

  static void showToast(Fragment fragment, final Toast toast) {
    if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
      toast.show();
    } else {
      runOnUiThread(fragment, new Runnable() {
        @Override
        public void run() {
          toast.show();
        }
      });
    }
  }

  static void showToast(final Fragment fragment, final int resId, final int duration) {
    runOnUiThread(fragment, new Runnable() {
      @Override
      public void run() {
        Toast.makeText(fragment.getContext(), resId, duration).show();
      }
    });
  }

  static void hideToast(final Fragment fragment, final Toast toast) {
    runOnUiThread(fragment, new Runnable() {
      @Override
      public void run() {
        toast.cancel();
      }
    });
  }
}
