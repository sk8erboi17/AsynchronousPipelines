package net.techtrends.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

/**
 * AsyncServerSocket class provides a method to create a server socket
 * that supports asynchronous communication.
 */

public class AsyncServerSocket {

    /**
     * Creates an instance of AsynchronousServerSocketChannel and binds it to the given address.
     *
     * @param inetSocketAddress The address to bind the server socket to.
     * @return An instance of AsynchronousServerSocketChannel.
     * @throws IOException If an I/O error occurs during the creation of the server socket.
     */
    public static AsynchronousServerSocketChannel createServer(InetSocketAddress inetSocketAddress) throws IOException {
        return AsynchronousServerSocketChannel.open().bind(inetSocketAddress);
    }

}
