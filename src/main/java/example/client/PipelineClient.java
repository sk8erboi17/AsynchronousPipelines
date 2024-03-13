package example.client;

import net.techtrends.listeners.output.AsyncOutputSocket;

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PipelineClient {
    private final static ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private final static AsynchronousSocketChannel httpChannel = AsyncOutputSocket.createOutput(new InetSocketAddress("localhost", 8080));
    private final static AsynchronousSocketChannel socketChannel = AsyncOutputSocket.createOutput(new InetSocketAddress("localhost", 8082));


    public static void main(String[] args) {
        setupOutput();
        scheduledExecutorService.scheduleWithFixedDelay(PipelineClient::setupInput, 0, 1, TimeUnit.MILLISECONDS);
    }

    private static void setupOutput() {
        PipeslineIO.buildPipelinesHttpOut(httpChannel);
        PipeslineIO.buildPipelinesSocketOut(socketChannel);
    }

    private static void setupInput() {
        PipeslineIO.buildPipelinesIn(socketChannel);
        PipeslineIO.buildPipelinesIn(httpChannel);
    }

}
