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
                buffer.clear();
                socketChannel.read(buffer);
                buffer.flip();
            }
        });
     thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
