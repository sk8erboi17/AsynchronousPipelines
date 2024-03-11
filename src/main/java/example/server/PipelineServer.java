package example.server;


import net.techtrends.Listener;
import net.techtrends.listeners.input.AsyncInputSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

public class PipelineServer {
    private static AsynchronousServerSocketChannel server;

    public static void main(String[] args) {
        try {
            server = AsyncInputSocket.createInput(new InetSocketAddress(8082));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        setupIncomeClients();
    }

    private static void setupIncomeClients() {
        Listener.getInstance().startConnectionListen(server, client -> {
            PipeslineIO.buildPipelinesIn(client);
            setupOutputForClients(client);
        });
    }

    private static void setupOutputForClients(AsynchronousSocketChannel client) {
        PipeslineIO.buildPipelinesOut(client);
    }


}
