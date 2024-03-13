package net.techtrends.listeners.group;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PipelineGroupManager {

    private final AsynchronousChannelGroup channelGroup;

    public PipelineGroupManager(int numThreads) {
        try {
            ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
            channelGroup = AsynchronousChannelGroup.withThreadPool(executorService);
        } catch (IOException e) {
            throw new RuntimeException("Unable to create AsynchronousChannelGroup", e);
        }
    }

    public AsynchronousSocketChannel createChannel(InetSocketAddress address) throws IOException, InterruptedException, ExecutionException {
        AsynchronousSocketChannel socketChannel = AsynchronousSocketChannel.open(channelGroup);
        socketChannel.connect(address).get();
        return socketChannel;
    }

}
