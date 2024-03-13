package net.techtrends.listeners.input;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class AsyncInputSocket {

    public static AsynchronousServerSocketChannel createInput(InetSocketAddress inetSocketAddress) throws IOException {
        try(AsynchronousServerSocketChannel client = AsynchronousServerSocketChannel.open().bind(inetSocketAddress)){
            return client;
        }
    }

}
