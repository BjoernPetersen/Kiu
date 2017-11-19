package com.github.bjoernpetersen.q.ui

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.inputmethod.InputMethodManager

fun AppCompatActivity.showKeyboard(view: View) {
  view.requestFocus()
  inputManager().showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

fun AppCompatActivity.hideKeyboard() {
  val focus = this.currentFocus
  if (focus != null)
    inputManager().hideSoftInputFromWindow(focus.windowToken, 0)
}

private fun AppCompatActivity.inputManager() =
    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
