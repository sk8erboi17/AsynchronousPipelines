package example.server;


import net.techtrends.Listener;
import net.techtrends.listeners.input.AsyncInputSocket;
import net.techtrends.listeners.output.AsyncChannelSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

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
            Runtime.getRuntime().addShutdownHook(new Thread(() -> AsyncChannelSocket.closeChannelSocketChannel(client)));
        });
    }

}
