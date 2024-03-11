package net.techtrends;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * This interface provides a callback to accept an incoming connection request.
 */
public interface ConnectionRequest {
    void acceptConnection(AsynchronousSocketChannel socketChannel) throws IOException;
}
