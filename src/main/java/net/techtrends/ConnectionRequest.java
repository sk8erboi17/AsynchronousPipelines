package net.techtrends;

import java.io.IOException;
import java.nio.channels.AsynchronousSocketChannel;


public interface ConnectionRequest {
    void acceptConnection(AsynchronousSocketChannel socketChannel);
}
