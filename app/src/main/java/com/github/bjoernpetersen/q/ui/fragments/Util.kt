@file:JvmName("Util")

package com.github.bjoernpetersen.q.ui.fragments

import android.os.Looper
import android.support.v4.app.Fragment
import android.widget.Toast

fun runOnUiThread(fragment: Fragment, runnable: Runnable) {
    val view = fragment.view
    view?.post(runnable)
}

fun showToast(fragment: Fragment, toast: Toast) {
    if (Looper.getMainLooper().thread === Thread.currentThread()) {
        toast.show()
    } else {
        runOnUiThread(fragment, Runnable { toast.show() })
    }
}

fun showToast(fragment: Fragment, resId: Int, duration: Int) {
    runOnUiThread(fragment, Runnable { Toast.makeText(fragment.context, resId, duration).show() })
}

fun hideToast(fragment: Fragment, toast: Toast) {
    runOnUiThread(fragment, Runnable { toast.cancel() })
}
