package example.client;

import net.techtrends.listeners.output.AsyncChannelSocket;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;

public class PipelineClient {
    private final static AsynchronousSocketChannel socketChannel = AsyncChannelSocket.createChannel(new InetSocketAddress("localhost", 8082));

    public static void main(String[] args) {
        setupOutput();
        setupInput();
    }

    private static void setupOutput() {
        PipeslineIO.buildPipelinesHttpOut();
        PipeslineIO.buildPipelinesSocketOut(socketChannel);
    }

    private static void setupInput() {
        PipeslineIO.buildPipelinesIn(socketChannel);
    }

}
