package com.github.bjoernpetersen.q.api

import android.util.Log
import com.hadisatrio.optional.function.Consumer
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.concurrent.Callable
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class HostDiscoverer @JvmOverloads constructor(private val onFinish: Consumer<String>? = null) : Runnable, Callable<String?> {

    private val lock: Lock
    private var result: String? = null

    init {
        this.lock = ReentrantLock()
    }

    override fun run() {
        call()
    }

    override fun call(): String? {
        if (result != null || !lock.tryLock()) {
            throw IllegalStateException()
        }
        try {
            result = autoDetect()
            onFinish(result)
            return result
        } finally {
            lock.unlock()
        }
    }

    private fun onFinish(result: String?) {
        onFinish?.consume(result)
    }

    private fun autoDetect(): String? {
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
        } catch (e: IOException) {
            Log.d(TAG, "Error auto detecting host", e)
            return null
        }
    }

    companion object {
        @JvmStatic
        private val TAG = HostDiscoverer::class.java.simpleName
        @JvmStatic
        private val GROUP_ADDRESS = "224.0.0.142"
        @JvmStatic
        private val PORT = 42945
    }
}
