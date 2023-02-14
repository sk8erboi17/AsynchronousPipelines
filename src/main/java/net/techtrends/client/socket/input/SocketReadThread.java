package net.techtrends.client.socket.input;

import net.techtrends.client.socket.SocketThreadIO;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class SocketReadThread extends SocketThreadIO {
    public SocketReadThread(AsynchronousSocketChannel socketChannel, int bytebuff) {
        super(socketChannel, bytebuff);
    }

    @Override
    public void startThread()  {
     Thread thread = new Thread(() -> {
            while (true) {
                try {
                    buffer.clear();
                    socketChannel.read(buffer).get();
                    buffer.flip();
                    System.out.println("Messagge from server: " + new String(buffer.array(), 0, buffer.limit()));

                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
            }
        });
     thread.start();

    }
}
