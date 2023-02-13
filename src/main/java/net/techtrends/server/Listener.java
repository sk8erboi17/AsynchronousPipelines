package net.techtrends.server;


import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Listener {
    private static final Listener instanceListener = new Listener();
    private final ExecutorService executors;
    public Listener() {
     executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);
    }

    /**
     * This method starts listening for incoming connection requests.
     *
     * @param serverSocketChannel The server socket channel to listen on.
     * @param connectionRequest   The callback to accept incoming connections.
     */
    public void startConnectionListen(AsynchronousServerSocketChannel serverSocketChannel, OnConnectionRequest connectionRequest) {
        executors.execute(() -> {
                try {
                    AsynchronousSocketChannel socketChannel = serverSocketChannel.accept().get();
                    connectionRequest.acceptConnection(socketChannel);

                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
        });


    }

    public static Listener getInstance() {
        return instanceListener;
    }

    public ExecutorService getExecutors() {
        return executors;
    }

}
