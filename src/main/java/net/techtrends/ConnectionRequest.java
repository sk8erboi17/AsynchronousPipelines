package net.techtrends;

import java.nio.channels.AsynchronousSocketChannel;


public interface ConnectionRequest {
    // Interface method to handle accepted connections
    void acceptConnection(AsynchronousSocketChannel socketChannel);
}
