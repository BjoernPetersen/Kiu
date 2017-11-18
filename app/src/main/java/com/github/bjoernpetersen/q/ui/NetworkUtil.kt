@file:JvmName("NetworkUtil")

package com.github.bjoernpetersen.q.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings
import android.support.v7.app.AlertDialog
import com.github.bjoernpetersen.q.R

fun Context.checkWifiState(context: Context = this) {
  if (context.isConnected()) return
  AlertDialog.Builder(context)
      .setMessage(R.string.wifi_disabled)
      .setPositiveButton(android.R.string.ok, { dialog, _ -> dialog.dismiss() })
      .setNeutralButton(R.string.connect_wifi, { dialog, _ -> dialog.dismiss(); connect() })
      .show()
}

private fun Context.connect() {
  val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
  try {
    startActivity(intent)
  } catch (ignored: ActivityNotFoundException) {
  }
}

private fun Context.isConnected(): Boolean {
  val manager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
  if (!manager.isWifiEnabled) return false
  return manager.connectionInfo.networkId != -1
}