@file:JvmName("Util")

package com.github.bjoernpetersen.q.ui

import android.os.Looper
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import java.util.*

private fun isUiThread(): Boolean = Looper.getMainLooper().thread === Thread.currentThread()

fun Fragment.runOnUiThread(runnable: () -> Unit) {
    if (!isUiThread()) this.view?.post(runnable)
    else runnable()
}

fun Fragment.showToast(resId: Int, duration: Int) =
        runOnUiThread { Toast.makeText(context, resId, duration).show() }

fun AppCompatActivity.showToast(resId: Int, duration: Int) =
        runOnUiThread { Toast.makeText(this, resId, duration).show() }

fun Int.asDuration(): String? {
    if (this == 0) {
        return null
    }
    val seconds = this % 60
    val minutes = (this - seconds) / 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}