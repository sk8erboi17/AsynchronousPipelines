package it.sk8erboi17.listeners.output;

import it.sk8erboi17.listeners.group.PipelineGroupManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

/**
 *  The Connection Management from client side
 *  This class is responsible for creating and managing AsynchronousSocketChannel instances on the client side.
 *  The method createChannel initializes a new AsynchronousSocketChannel and connects it to a server address (InetSocketAddress).
 *  It uses PipelineGroupManager to manage the underlying channel group, which can help in efficiently handling multiple concurrent connections.
 */
public class AsyncChannelSocket {
    private static final PipelineGroupManager pipelineGroupManager = new PipelineGroupManager(Runtime.getRuntime().availableProcessors());

    // Static method to create an AsynchronousSocketChannel and connect it to the given InetSocketAddress.
    public static AsynchronousSocketChannel createChannel(InetSocketAddress inetSocketAddress) {
        // Create a PipelineGroupManager with a thread pool size equal to half the available processors.
        AsynchronousSocketChannel socketChannel;

        try {
            // Use the PipelineGroupManager to create and connect the AsynchronousSocketChannel.
            socketChannel = pipelineGroupManager.createChannel(inetSocketAddress);
        } catch (IOException | ExecutionException | InterruptedException e) {
            // If an exception occurs during the creation or connection of the channel, wrap it in a RuntimeException and rethrow it.
            throw new RuntimeException("Error while opening socket channel: " + e.getMessage(), e);
        }

        return socketChannel;
    }

    // Static method to close an AsynchronousSocketChannel if it is open.
    public static void closeChannelSocketChannel(AsynchronousSocketChannel socketChannel) {
        // Check if the socket channel is not null and is open.
        if (socketChannel != null && socketChannel.isOpen()) {
            try {
                // Attempt to close the socket channel.
                socketChannel.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to close socket channel", e);
            }
        }
    }


    /**
     * Shuts down the underlying static PipelineGroupManager, releasing all associated resources.
     * This method MUST be called once when the entire application using AsyncChannelSocket is shutting down,
     * not when individual channels are closed.
     */
    public static void shutdown() {
        pipelineGroupManager.shutdown();
    }

}
