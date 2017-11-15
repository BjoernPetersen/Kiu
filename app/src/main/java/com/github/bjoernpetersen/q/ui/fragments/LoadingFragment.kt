package com.github.bjoernpetersen.q.ui.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.github.bjoernpetersen.q.R

/**
 * A simple fragment showing a loading animation.
 */
class LoadingFragment : Fragment() {

  override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
      savedInstanceState: Bundle?): View? =
      inflater?.inflate(R.layout.fragment_loading, container, false)

  override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
    view?.findViewById<View>(R.id.progress)?.animate()
  }
}
