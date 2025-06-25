package it.sk8erboi17;



import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Listener class is designed to handle incoming client connections to a server.
 * It utilizes asynchronous I/O provided by Java NIO to accept new connections and manage them using a callback mechanism.
 */
public class Listener {
    private static final Listener instanceListener = new Listener();
    private final ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);

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
                throw new RuntimeException("Error with connection " + exc.getMessage(), exc);
            }
        }));
    }
}
