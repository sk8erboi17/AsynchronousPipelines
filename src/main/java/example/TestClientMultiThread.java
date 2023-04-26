package example;

import net.techtrends.client.AsyncSocket;
import net.techtrends.general.listeners.output.OutputListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class TestClientMultiThread {

    public static void main(String[] args) {
        try {
            // Create a new AsynchronousSocketChannel that connects to a server at localhost:8080
            AsynchronousSocketChannel socketChannel = AsyncSocket.createClient(new InetSocketAddress("localhost", 8080));

            // Create a new OutputListener to handle the socket output
            OutputListener listener = new OutputListener(socketChannel);

            // Create a new thread that will send 600 messages to the server
            new Thread(() -> {
                for (int i = 0; i < 600; i++) {
                    System.out.println("Test n " + i);
                    // Send the message to the server using the OutputListener
                    listener.handle(false, "Test n " + i);
                    try {
                        Thread.sleep(3);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

            // Create a new thread that will send 6000 messages to the server
            new Thread(() -> {
                for (int i = 0; i < 6000; i++) {
                    System.out.println(i);
                    // Send the message to the server using the OutputListener
                    listener.handle(false, i);
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();

        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
