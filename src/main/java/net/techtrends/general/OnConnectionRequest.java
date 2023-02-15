package net.techtrends.general;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * This interface provides a callback to accept an incoming connection request.
 */
public interface OnConnectionRequest {
    /**
     * This method is called whenever a new connection is requested.
     *
     * @param socketChannel The socket channel that represents the connection.
     */
    void acceptConnection(AsynchronousSocketChannel socketChannel);
}
