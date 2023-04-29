package example;

import net.techtrends.client.AsyncSocket;
import net.techtrends.general.listeners.ResponseCallback;
import net.techtrends.general.listeners.output.OutputListener;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * The TestClientMultiThread class creates a client that connects to a server at localhost:8080 using the AsyncSocket class.
 * <p>
 * It then creates single thread that send string values to the server using an OutputListener.
 * Each thread sends a different number of values at different time intervals.
 * The ResponseCallback interface is implemented to handle success and failure for each of the values sent.
 * <p>
 * The createResponseCallback method returns a new instance of ResponseCallback with a custom implementation for success and failure.
 * The sleep method is used to delay the sending of values in each thread.
 * <p>
 * The class is intended to demonstrate how to use the OutputListener class in a multi-threaded environment to send data to a server asynchronously.
 */

public class TestClientSingleThread {

    public static void main(String[] args) {
        try {
            AsynchronousSocketChannel socketChannel = AsyncSocket.createClient(new InetSocketAddress("localhost", 8080));
            OutputListener listener = new OutputListener(socketChannel, 2048,true);

            for (int i = 0; i < 500; i++) {
                String message = "Test".concat(String.valueOf(i));
                System.out.println(message);
                listener.sendStringSanitized(message, new ResponseCallback() {
                    @Override
                    public void complete(Object o) {
                        System.out.println("SUCCESS");
                    }

                    @Override
                    public void completeExceptionally(Throwable throwable) {
                        System.out.println("FAILED");
                    }
                });
                Thread.sleep(30);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
