package net.techtrends.general;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * This interface provides a callback to accept an incoming connection request.
 */
public interface ConnectionRequest {
    void acceptConnection(AsynchronousSocketChannel socketChannel);
}
