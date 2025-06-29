package example.server;

import it.sk8erboi17.Listener;
import it.sk8erboi17.listeners.group.PipelineGroupManager;
import it.sk8erboi17.listeners.input.AsyncInputSocket;
import it.sk8erboi17.listeners.output.AsyncChannelSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class PipelineServer {
    private static final Logger log = LoggerFactory.getLogger(PipelineServer.class);
    private static AsynchronousServerSocketChannel server;

    public static void main(String[] args) {
        try {
            // Create and initialize an AsynchronousServerSocketChannel to listen for incoming client connections
            server = AsyncInputSocket.createInput(new InetSocketAddress(8082));

        } catch (IOException e) {
            // Handle any IOExceptions thrown while creating the server socket
            throw new RuntimeException(e);
        }

        closeServer();

        // Set up the server to handle incoming client connections
        setupIncomeClients();
    }

    private static void setupIncomeClients() {
        // Get the singleton instance of the Listener class
        Listener.getInstance().startConnectionListen(server, client -> {
            // Build input pipelines for the connected client
            PipeslineIO.buildPipelinesIn(client);

            // Add a shutdown hook to close the client channel when the application terminates
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                AsyncChannelSocket.closeChannelSocketChannel(client);

            }));
        });
    }

    private static void closeServer(){
        Runtime.getRuntime().addShutdownHook(new Thread(() ->{
            Listener.closeListener();
            try {
                server.close();
            } catch (IOException e) {
                log.error("Something goes wrong {}" , e.getMessage(), e);
            }
        }));
    }
}
