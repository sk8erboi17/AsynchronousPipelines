package net.techtrends;


import net.techtrends.exception.ServerExceptionHandler;

import java.io.IOException;
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

    public static Listener getInstance() {
        return instanceListener;
    }

    public void startConnectionListen(AsynchronousServerSocketChannel serverSocketChannel, ConnectionRequest connectionRequest) {
        executors.execute(() -> serverSocketChannel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
            @Override
            public void completed(AsynchronousSocketChannel socketChannel, Void attachment) {
                try {
                    connectionRequest.acceptConnection(socketChannel);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
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
        }));
    }

}
