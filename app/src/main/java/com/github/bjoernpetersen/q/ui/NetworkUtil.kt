@file:JvmName("NetworkUtil")

package com.github.bjoernpetersen.q.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.provider.Settings
import android.support.v7.app.AlertDialog
import com.github.bjoernpetersen.q.R

fun checkWifiState(context: Context) {
  if (isConnected(context)) return
  AlertDialog.Builder(context)
      .setMessage(R.string.wifi_disabled)
      .setPositiveButton(android.R.string.ok, { dialog, which -> dialog.dismiss() })
      .setNeutralButton(R.string.connect_wifi, { dialog, which -> connect(context) })
      .show()
}

private fun connect(context: Context) {
  val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
  try {
    context.startActivity(intent)
  } catch (ignored: ActivityNotFoundException) {
  }
}

private fun isConnected(context: Context): Boolean {
  val manager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
  if (!manager.isWifiEnabled) return false
  return manager.connectionInfo.networkId != -1
}