package it.sk8erboi17;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * The Listener class is designed to handle incoming client connections to a server.
 * It utilizes asynchronous I/O provided by Java NIO to accept new connections and manage them using a callback mechanism.
 */
public class Listener {
    private static final Listener instanceListener = new Listener();
    private static final Logger log = LoggerFactory.getLogger(Listener.class);
    private static final ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);

    public static Listener getInstance() {
        return instanceListener;
    }

    public void startConnectionListen(AsynchronousServerSocketChannel serverSocketChannel, ConnectionRequest connectionRequest) {
        // Executes the asynchronous accept method on a separate thread
        executors.execute(() -> serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
                connectionRequest.acceptConnection(socketChannel);
                serverSocketChannel.accept(attachment, this);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                log.error("Error with connection {}", exc.getMessage(), exc);
            }
        }));
    }

    public static void closeListener(){
        if (executors != null && !executors.isShutdown()) {
            executors.shutdown();
            try {
                // Attendi che tutti i task in sospeso vengano completati, con un timeout
                if (!executors.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Executor service did not terminate in time. Forcing shutdown.");
                    executors.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Executor service shutdown interrupted: " + e.getMessage());
            }
        }
    }
}
