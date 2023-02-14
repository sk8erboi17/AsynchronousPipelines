package net.techtrends.server;


import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
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
        executors.execute(() ->{
        serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
                // la connessione è stata accettata con successo
                connectionRequest.acceptConnection(socketChannel);
                serverSocketChannel.accept(null, this); // accetta la prossima connessione
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                // la connessione ha fallito
                throw new RuntimeException(exc);
            }
        });
    });
    }


    public static Listener getInstance() {
        return instanceListener;
    }

    public ExecutorService getExecutors() {
        return executors;
    }

}
