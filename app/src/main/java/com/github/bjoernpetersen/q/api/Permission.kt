package com.github.bjoernpetersen.q.api

import android.os.Parcel
import android.os.Parcelable

enum class Permission(val label: String) : Parcelable {
  SKIP("skip"), DISLIKE("dislike"), MOVE("move");

  override fun writeToParcel(dest: Parcel, flags: Int) = dest.writeString(name)

  override fun describeContents(): Int = 0

  companion object {
    @JvmStatic
    fun matchByLabel(label: String): Permission {
      values().filter { it.label == label }
          .forEach { return it }
      throw IllegalArgumentException("Unknown permission: " + label)
    }

    @JvmStatic
    private operator fun get(parcel: Parcel): Permission {
      val name = parcel.readString()
      return matchByLabel(name)
    }

    @JvmStatic
    val CREATOR: Parcelable.Creator<Permission> = object : Parcelable.Creator<Permission> {
      override fun createFromParcel(parcel: Parcel): Permission = Permission[parcel]
      override fun newArray(size: Int): Array<Permission?> = arrayOfNulls(size)
    }
  }
}
