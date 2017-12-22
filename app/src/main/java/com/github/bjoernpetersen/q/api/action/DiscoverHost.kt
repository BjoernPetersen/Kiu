package com.github.bjoernpetersen.q.api.action

import android.content.Context
import android.net.wifi.WifiManager
import android.util.Log
import com.github.bjoernpetersen.q.api.Config
import com.github.bjoernpetersen.q.tag
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.toSingle
import io.reactivex.schedulers.Schedulers
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.concurrent.Callable

private const val GROUP_ADDRESS = "224.0.0.142"
private const val PORT = 42945
private const val LOCK_TAG = "kiu_broadcast"

class DiscoverHost(private val context: Context) : Callable<String> {
  @Throws(IOException::class)
  override fun call(): String {
    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val lock = wifiManager.createMulticastLock(LOCK_TAG)
    lock.acquire()
    try {
      MulticastSocket(PORT).use { socket ->
        val groupAddress = InetAddress.getByName(GROUP_ADDRESS)
        socket.joinGroup(groupAddress)
        socket.soTimeout = 4000
        val buffer = ByteArray(8)
        val packet = DatagramPacket(buffer, buffer.size)
        socket.broadcast = true
        socket.receive(packet)
        socket.leaveGroup(groupAddress)
        return packet.address.hostAddress
      }
    } finally {
      lock.release()
    }
  }

  fun defaultAction(alsoOnSuccess: (String) -> Unit = {},
      alsoOnError: (Throwable) -> Unit = {}): Disposable = toSingle()
      .subscribeOn(Schedulers.io())
      .observeOn(AndroidSchedulers.mainThread())
      .subscribe({
        if (Config.host != it) {
          Log.i(tag(), "Found new host: " + it)
          Config.host = it
        }
        alsoOnSuccess(it)
      }, { Log.v(tag(), "Could not retrieve host", it); alsoOnError(it) })
}
