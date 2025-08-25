package it.sk8erboi17;

import it.sk8erboi17.utils.FailWriter;
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
 * The CompletionHandler for accepting connections is now a static inner class to avoid repeated object creation.
 */
public class Listener {
    private static final Listener instanceListener = new Listener();
    private static final Logger log = LoggerFactory.getLogger(Listener.class);
    private static final ExecutorService executors = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() / 2);

    /**
     * A SINGLE, STATIC, and REUSABLE CompletionHandler for accepting connections.
     * It is stateless and does not depend on an instance of the Listener class.
     */
    private static final CompletionHandler<AsynchronousSocketChannel, ConnectionRequest> acceptCompletionHandler = new CompletionHandler<>() {
        @Override
        public void completed(AsynchronousSocketChannel socketChannel, ConnectionRequest attachment) {
            try {
                if (attachment != null) {
                    attachment.acceptConnection(socketChannel, null);
                }
            } catch (Exception e) {
                // Gestisce gli errori all'interno di acceptConnection per un flusso di lavoro coerente
                failed(e, attachment);
                return;
            }

            // Chiedi al serverSocketChannel di accettare una nuova connessione.
            // Utilizza l'istanza del Listener per ottenere il canale.
            AsynchronousServerSocketChannel serverChannel = Listener.getInstance().getServerSocketChannel();
            if (serverChannel != null && serverChannel.isOpen()) {
                serverChannel.accept(attachment, this);
            }
        }

        @Override
        public void failed(Throwable exc, ConnectionRequest attachment) {
            log.error("Error with connection {}", exc.getMessage(), exc);
            FailWriter.writeFile("Error with connection ", exc);

            if (attachment != null) {
                attachment.onConnectionFailed(exc);
            }
        }
    };

    private AsynchronousServerSocketChannel serverSocketChannel;

    public void startConnectionListen(AsynchronousServerSocketChannel serverSocketChannel, ConnectionRequest connectionRequest) {
        this.serverSocketChannel = serverSocketChannel;

        // Passa l'istanza di ConnectionRequest come attachment per risolvere il problema di concorrenza.
        executors.execute(() -> serverSocketChannel.accept(connectionRequest, acceptCompletionHandler));
    }

    public static void closeListener(){
        if (executors != null && !executors.isShutdown()) {
            executors.shutdown();
            try {
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

    public AsynchronousServerSocketChannel getServerSocketChannel() {
        return serverSocketChannel;
    }

    public static Listener getInstance() {
        return instanceListener;
    }
}
