package net.techtrends;

import java.nio.channels.AsynchronousSocketChannel;


public interface ConnectionRequest {
    void acceptConnection(AsynchronousSocketChannel socketChannel);
}
