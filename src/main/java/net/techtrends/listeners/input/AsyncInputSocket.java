package net.techtrends.listeners.input;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class AsyncInputSocket {

    public static AsynchronousServerSocketChannel createInput(InetSocketAddress inetSocketAddress) throws IOException {
        return AsynchronousServerSocketChannel.open().bind(inetSocketAddress);
    }

}
