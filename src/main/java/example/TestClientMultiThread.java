package example;

import net.techtrends.client.AsyncSocket;
import net.techtrends.general.listeners.ResponseCallback;
import net.techtrends.general.listeners.output.OutputListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

/**
 * The TestClientMultiThread class creates a client that connects to a server at localhost:8080 using the AsyncSocket class.
 * <p>
 * It then creates two threads that send integer values to the server using an OutputListener.
 * Each thread sends a different number of values at different time intervals.
 * The ResponseCallback interface is implemented to handle success and failure for each of the values sent.
 * <p>
 * The createResponseCallback method returns a new instance of ResponseCallback with a custom implementation for success and failure.
 * The sleep method is used to delay the sending of values in each thread.
 * <p>
 * The class is intended to demonstrate how to use the OutputListener class in a multi-threaded environment to send data to a server asynchronously.
 */
public class TestClientMultiThread {

    public static void main(String[] args) {
        try {
            AsynchronousSocketChannel socketChannel = AsyncSocket.createClient(new InetSocketAddress("localhost", 8080));
            OutputListener outputListener1 = new OutputListener(socketChannel, 2048,true);
            OutputListener outputListener2 = new OutputListener(socketChannel, 2048,true);

            Runnable thread1 = () -> {
                for (int i = 0; i < 600; i++) {
                    System.out.println("Test n " + i);
                    sleep(100);
                    outputListener1.sendInt(i,createResponseCallback("Thread 1"));
                }
            };

            Runnable thread2 = () -> {
                for (int i = 0; i < 6000; i++) {
                    System.out.println(i);
                    outputListener2.sendInt(i,createResponseCallback("Thread 2"));
                    sleep(200);
                }
            };

            new Thread(thread1, "Thread #1").start();
            new Thread(thread2, "Thread #2").join();

        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static ResponseCallback createResponseCallback(String threadName) {
        return new ResponseCallback() {
            @Override
            public void complete(Object o) {
                System.out.println(threadName + " - SUCCESS");
            }

            @Override
            public void completeExceptionally(Throwable throwable) {
                System.out.println(threadName + " - FAILED");
                throwable.printStackTrace();
            }
        };
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
