package example.server;

import it.sk8erboi17.Listener;
import it.sk8erboi17.listeners.input.AsyncInputSocket;
import it.sk8erboi17.listeners.output.AsyncChannelSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class PipelineServer {
    private static AsynchronousServerSocketChannel server;

    public static void main(String[] args) {
        try {
            // Create and initialize an AsynchronousServerSocketChannel to listen for incoming client connections
            server = AsyncInputSocket.createInput(new InetSocketAddress(8082));
        } catch (IOException e) {
            // Handle any IOExceptions thrown while creating the server socket
            throw new RuntimeException(e);
        }

        // Set up the server to handle incoming client connections
        setupIncomeClients();
    }

    private static void setupIncomeClients() {
        // Get the singleton instance of the Listener class
        Listener.getInstance().startConnectionListen(server, client -> {
            // Build input pipelines for the connected client
            PipeslineIO.buildPipelinesIn(client);

            // Add a shutdown hook to close the client channel when the application terminates
            Runtime.getRuntime().addShutdownHook(new Thread(() -> AsyncChannelSocket.closeChannelSocketChannel(client)));
        });
    }
}
