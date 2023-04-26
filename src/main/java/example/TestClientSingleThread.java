package example;

import net.techtrends.client.AsyncSocket;
import net.techtrends.general.listeners.output.OutputListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class TestClientSingleThread {

    public static void main(String[] args) {
        try {
            // Create a socket channel and connect to the server
            AsynchronousSocketChannel socketChannel = AsyncSocket.createClient(new InetSocketAddress("localhost", 8080));

            // Create an output listener to handle the server responses
            OutputListener listener = new OutputListener(socketChannel);

            // Send 10000 messages to the server and wait for a response
            for (int i = 0; i < 10000; i++) {
                System.out.println(i);
                listener.handle(false, i);
                Thread.sleep(3);
            }
        } catch (IOException | ExecutionException | InterruptedException e) {
            // Handle any exceptions that occur
            e.printStackTrace();
        }
    }
}
