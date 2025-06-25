package example.client;

import it.sk8erboi17.listeners.output.AsyncChannelSocket;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;

public class PipelineClient {
    // Create a static AsynchronousSocketChannel connected to the server at localhost on port 8082
    private final static AsynchronousSocketChannel socketChannel = AsyncChannelSocket.createChannel(new InetSocketAddress("localhost", 8082));

    public static void main(String[] args) {
        // Initialize output and input pipelines
        setupOutput();
        setupInput();

        // Add a shutdown hook to close the socket channel when the application terminates
        Runtime.getRuntime().addShutdownHook(new Thread(() -> AsyncChannelSocket.closeChannelSocketChannel(socketChannel)));
    }

    // Sets up the output pipeline for sending data to the server
    private static void setupOutput() {
        PipeslineIO.buildPipelinesSocketOut(socketChannel);
    }

    // Sets up the input pipeline for receiving data from the server
    private static void setupInput() {
        PipeslineIO.buildPipelinesIn(socketChannel);
    }
}
