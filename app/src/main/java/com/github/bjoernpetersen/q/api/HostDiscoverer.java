package com.github.bjoernpetersen.q.api;

import android.util.Log;
import com.hadisatrio.optional.function.Consumer;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class HostDiscoverer implements Runnable, Callable<String> {

  private static final String TAG = HostDiscoverer.class.getSimpleName();
  private static final String GROUP_ADDRESS = "224.0.0.142";
  private static final int PORT = 42945;

  private final Lock lock;
  private final Consumer<String> onFinish;
  private String result;

  public HostDiscoverer() {
    this(null);
  }

  public HostDiscoverer(Consumer<String> onFinish) {
    this.lock = new ReentrantLock();
    this.onFinish = onFinish;
  }

  @Override
  public void run() {
    call();
  }

  @Override
  public String call() {
    if (result != null || !lock.tryLock()) {
      throw new IllegalStateException();
    }
    try {
      result = autoDetect();
      onFinish(result);
      return result;
    } finally {
      lock.unlock();
    }
  }

  private void onFinish(String result) {
    if (onFinish != null) {
      onFinish.consume(result);
    }
  }

  private String autoDetect() {
    try (MulticastSocket socket = new MulticastSocket(PORT)) {
      InetAddress groupAddress = InetAddress.getByName(GROUP_ADDRESS);
      socket.joinGroup(groupAddress);
      socket.setSoTimeout(4000);
      byte[] buffer = new byte[8];
      DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
      socket.setBroadcast(true);
      socket.receive(packet);
      socket.leaveGroup(groupAddress);
      return packet.getAddress().getHostAddress();
    } catch (IOException e) {
      Log.d(TAG, "Error auto detecting host", e);
      return null;
    }
  }
}
