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

    public static AsynchronousSocketChannel createClient(InetSocketAddress inetSocketAddress) throws IOException, ExecutionException, InterruptedException {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open();
        Future<Void> connectFuture = socketChannel.connect(inetSocketAddress);
        connectFuture.get();
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
