package net.techtrends.listeners.group;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PipelineGroupManager {

    // Manages a group of asynchronous channels, sharing a thread pool.
    private final AsynchronousChannelGroup channelGroup;

    public PipelineGroupManager(int numThreads) {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
            // Initialize the AsynchronousChannelGroup with the thread pool.
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create AsynchronousChannelGroup", e);
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
}
