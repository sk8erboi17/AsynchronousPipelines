package net.techtrends.listeners.output;

import net.techtrends.listeners.group.PipelineGroupManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class AsyncOutputSocket {

    public static AsynchronousSocketChannel createOutput(InetSocketAddress inetSocketAddress) {
        PipelineGroupManager pipelineGroupManager = new PipelineGroupManager(Runtime.getRuntime().availableProcessors() / 2);
        AsynchronousSocketChannel socketChannel;
        try {
            socketChannel = pipelineGroupManager.createChannel(inetSocketAddress);
        } catch (IOException | ExecutionException | InterruptedException e) {
            throw new RuntimeException("Error while opening socket channel: " + e.getMessage(), e);
        }
        return socketChannel;
    }

    public static void closeOutputSocketChannel(AsynchronousSocketChannel socketChannel) {

        if (socketChannel != null && socketChannel.isOpen()) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to close socket channel", e);
            }
        }
    }
}
