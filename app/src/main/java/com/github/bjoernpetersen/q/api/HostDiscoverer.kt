package com.github.bjoernpetersen.q.api

import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.util.concurrent.Callable

private const val GROUP_ADDRESS = "224.0.0.142"
private const val PORT = 42945

class HostDiscoverer : Callable<String> {
  @Throws(IOException::class)
  override fun call(): String {
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
  }
}
