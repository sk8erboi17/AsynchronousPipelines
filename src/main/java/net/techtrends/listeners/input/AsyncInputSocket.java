package net.techtrends.listeners.input;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

/**
 *  *  The Connection Management from server side
 * This class provides a utility method for creating and binding an
 * AsynchronousServerSocketChannel to a specified network address (InetSocketAddress).
 * This channel listens for incoming connection requests from clients.
 * The method createInput opens a new server socket channel and binds it to the given address
 * , preparing it to accept client connections.
 */
public class AsyncInputSocket {

    // Static method to create an AsynchronousServerSocketChannel and bind it to the given InetSocketAddress.
    public static AsynchronousServerSocketChannel createInput(InetSocketAddress inetSocketAddress) throws IOException {
        return AsynchronousServerSocketChannel.open().bind(inetSocketAddress);
    }

}
