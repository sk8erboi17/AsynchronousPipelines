package it.sk8erboi17.listeners.group;

import it.sk8erboi17.utils.FailWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PipelineGroupManager {

    private static final Logger log = LoggerFactory.getLogger(PipelineGroupManager.class);
    // Manages a group of asynchronous channels, sharing a thread pool.
    private AsynchronousChannelGroup channelGroup;
    private ExecutorService executorService;

    public PipelineGroupManager(int numThreads)  {
        try {
            executorService = Executors.newFixedThreadPool(numThreads);
            // Initialize the AsynchronousChannelGroup with the thread pool.
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
        } catch (IOException e) {
            log.error("Error with the pipeline group manager :  {}" , e.getMessage() , e);
            FailWriter.writeFile("Error with the pipeline group manager :  " , e);
        }
    }


    // Creates a new AsynchronousSocketChannel and connects it to the given address.
    public AsynchronousSocketChannel createChannel(InetSocketAddress address) throws IOException, InterruptedException, ExecutionException {
        // Open a new AsynchronousSocketChannel in the context of the channel group.
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(channelGroup);
        // Connect to the specified address and wait for the connection to complete.
        socketChannel.connect(address).get();
        return socketChannel;
    }

    /**
     * Shuts down the executor service and the channel group managed by this instance.
     * This method should be called when the application is shutting down to release resources.
     */
    public void shutdown() {
        if (channelGroup != null && !channelGroup.isShutdown()) {
            try {
                channelGroup.shutdown();
                // Await termination of the channel group, allowing existing tasks to complete.
                if (!channelGroup.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.info("Channel group did not terminate in time.");
                    //TODO add logging info of the  state of channel group
                    channelGroup.shutdownNow();
                }
            } catch (IOException e) {
                log.error("Error while shutting down channel group: {}", e.getMessage());
                FailWriter.writeFile("Error while shutting down channel group: ", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Re-interrupt the current thread
                log.error("Channel group shutdown interrupted: {}", e.getMessage());
                FailWriter.writeFile("Channel group shutdown interrupted: ", e);
            }
        }

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
            try {
                // Attendi che tutti i task in sospeso vengano completati, con un timeout
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    log.error("Executor service did not terminate in time. Forcing shutdown.");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Executor service shutdown interrupted: {}", e.getMessage());
                FailWriter.writeFile("Executor service shutdown interrupted: ", e);
            }
        }
    }
}
