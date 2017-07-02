package com.github.bjoernpetersen.q.ui.fragments;

import android.support.v4.app.Fragment;
import android.view.View;

final class Util {

  private Util() {
  }

  static void runOnUiThread(Fragment framgent, Runnable runnable) {
    View view = framgent.getView();
    if (view != null) {
      view.post(runnable);
    }
  }
}
