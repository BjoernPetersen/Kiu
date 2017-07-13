package com.github.bjoernpetersen.q.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

final class Util {

  private Util() {
  }

  static void runOnUiThread(Fragment fragment, Runnable runnable) {
    View view = fragment.getView();
    if (view != null) {
      view.post(runnable);
    }
  }
}
