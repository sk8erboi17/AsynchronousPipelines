package it.sk8erboi17;

import java.nio.channels.AsynchronousSocketChannel;


public interface ConnectionRequest {
    // Interface method to handle accepted connections
    void acceptConnection(AsynchronousSocketChannel socketChannel);
}
