package net.techtrends.listeners.output;

import net.techtrends.listeners.group.PipelineNetworkManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class AsyncOutputSocket {

    public static AsynchronousSocketChannel createOutput(InetSocketAddress inetSocketAddress) {
        PipelineNetworkManager pipelineNetworkManager = new PipelineNetworkManager(Runtime.getRuntime().availableProcessors() / 2);
        AsynchronousSocketChannel socketChannel;
        try {
            socketChannel = pipelineNetworkManager.createChannel(inetSocketAddress);
        } catch (IOException e) {
            throw new RuntimeException("Error while opening socket channel: " + e.getMessage(), e);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        return socketChannel;
    }

    public static void closeOutputSocketChannel(AsynchronousSocketChannel socketChannel) {

        if (socketChannel != null && socketChannel.isOpen()) {
            try {
                socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
