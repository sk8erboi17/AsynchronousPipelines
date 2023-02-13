package net.techtrends.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * AsyncSocket is an abstract class that provides a static method for creating a client connection using an AsynchronousSocketChannel.
 */
public abstract class AsyncSocket {

    /**
     * Creates a client connection using the specified InetSocketAddress and returns an AsynchronousSocketChannel.
     *
     * @param inetSocketAddress the address and port of the server to connect to
     * @return an AsynchronousSocketChannel that represents the connected client
     * @throws IOException if an I/O error occurs
     * @throws ExecutionException if the operation cannot be completed
     * @throws InterruptedException if the current thread was interrupted while waiting
     */
    public static AsynchronousSocketChannel createClient(InetSocketAddress inetSocketAddress) throws IOException, ExecutionException, InterruptedException {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
        Future<Void> connectFuture = socketChannel.connect(inetSocketAddress);
        connectFuture.get();
        return socketChannel;
    }

}
