package net.techtrends.general;


import net.techtrends.general.exception.ServerExceptionHandler;

import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Listener {
    private static final Listener instanceListener = new Listener();
    private final ExecutorService executors;

    public Listener() {


        executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2, r -> {
            Thread t = new Thread(r);
            t.setUncaughtExceptionHandler(new ServerExceptionHandler());
            return t;
        });
    }

    /**
     * This method starts listening for incoming connection requests.
     *
     * @param serverSocketChannel The server socket channel to listen on.
     * @param connectionRequest   The callback to accept incoming connections.
     */
    public void startConnectionListen(AsynchronousServerSocketChannel serverSocketChannel, ConnectionRequest connectionRequest) {
        executors.execute(() -> {
            serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
                    connectionRequest.acceptConnection(socketChannel);
                    serverSocketChannel.accept(null, this);
                }

                @Override
                public void failed(Throwable exc, Void attachment) {
                    try {
                        exc.printStackTrace();
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }


    public static Listener getInstance() {
        return instanceListener;
    }

}
