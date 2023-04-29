package net.techtrends.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;


/**
 * AsyncSocket is class that provides a static method for creating and close a client connection using an AsynchronousSocketChannel.
 */
public class AsyncSocket {

    public static AsynchronousSocketChannel createClient(InetSocketAddress inetSocketAddress) {
        AsynchronousSocketChannel socketChannel = null;
        try {
            socketChannel = AsynchronousSocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException("Error while opening socket channel: " + e.getMessage(), e);
        }
        Future<Void> connectFuture = socketChannel.connect(inetSocketAddress);
        try {
            connectFuture.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore the interrupt flag of the thread
            throw new RuntimeException("Connection interrupted while connecting to server: " + e.getMessage(), e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Error while connecting to server: " + e.getMessage(), e);
        }
        return socketChannel;
    }

    public static void closeSocketChannel(AsynchronousSocketChannel socketChannel) {
        if(socketChannel != null && socketChannel.isOpen()) {
            try {
                socketChannel.shutdownInput();
            } catch (IOException e) {
                System.err.println("Failed to shutdown input socket channel: " + e.getMessage());
            }
            try {
                socketChannel.shutdownOutput();
            } catch (IOException e) {
                System.err.println("Failed to shutdown output socket channel: " + e.getMessage());
            }
            try {
                socketChannel.close();
            } catch (IOException e) {
                System.err.println("Failed to close socket channel: " + e.getMessage());
            }
        }
    }


}
