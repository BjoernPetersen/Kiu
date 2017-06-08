package com.github.bjoernpetersen.q.api;

import android.os.Parcel;
import android.os.Parcelable;

enum Permission implements Parcelable {
  SKIP("skip"), DISLIKE("dislike");

  private final String name;

  Permission(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public static Permission matchByName(String name) {
    for (Permission permission : values()) {
      if (permission.getName().equals(name)) {
        return permission;
      }
    }
    throw new IllegalArgumentException("Unknown permission: " + name);
  }

  private static Permission get(Parcel in) {
    String name = in.readString();
    return matchByName(name);
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(name);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<Permission> CREATOR = new Creator<Permission>() {
    @Override
    public Permission createFromParcel(Parcel in) {
      return Permission.get(in);
    }

    @Override
    public Permission[] newArray(int size) {
      return new Permission[size];
    }
  };
}
