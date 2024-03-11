package net.techtrends.listeners.output;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AsyncOutputSocket {

    public static AsynchronousSocketChannel createOutput(InetSocketAddress inetSocketAddress) {
        AsynchronousSocketChannel socketChannel;
        try {
            socketChannel = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException("Error while opening socket channel: " + e.getMessage(), e);
        }
        Future<Void> connectFuture = socketChannel.connect(inetSocketAddress);
        try {
            connectFuture.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Connection interrupted while connecting to server: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error while connecting to server: " + e.getMessage(), e);
        }
        return socketChannel;
    }

    public static void closeOutputSocketChannel(AsynchronousSocketChannel socketChannel) {

        if (socketChannel != null && socketChannel.isOpen()) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
