package com.github.bjoernpetersen.q.ui.fragments

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.view.ViewGroup

abstract class CachedFragmentPagerAdapter<out T : Fragment>(
    private val fragmentManager: FragmentManager, size: Int) :
    FragmentPagerAdapter(fragmentManager) {

  private val tags: Array<String?> = Array(size, { null })

  @Suppress("UNCHECKED_CAST")
  fun getFragment(position: Int): T? = tags[position]?.let {
    fragmentManager.findFragmentByTag(it) as? T
  }

  override fun instantiateItem(container: ViewGroup?, position: Int): Any =
      super.instantiateItem(container, position).also {
        tags[position] = (it as Fragment).tag
      }
}