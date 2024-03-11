package example.client;

import net.techtrends.listeners.output.AsyncOutputSocket;
import net.techtrends.network.pipeline.out.PipelineOut;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PipelineClient {
    private final static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final static AsynchronousSocketChannel socketChannel = AsyncOutputSocket.createOutput(new InetSocketAddress("localhost", 8080));

    public static void main(String[] args) {
        setupOutput();
        scheduledExecutorService.scheduleWithFixedDelay(PipelineClient::setupInput, 50, 50, TimeUnit.MILLISECONDS);
    }

    private static void setupOutput() {
        PipeslineIO.buildPipelinesOut(socketChannel);
    }

    private static void setupInput() {
        PipeslineIO.buildPipelinesIn(socketChannel);
    }

}
