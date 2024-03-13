package example.server;

import example.server.request.SayHelloToClient;
import net.techtrends.listeners.response.Callback;
import net.techtrends.listeners.response.CallbackBuilder;
import net.techtrends.network.pipeline.in.PipelineInBuilder;
import net.techtrends.network.pipeline.out.PipelineOut;
import net.techtrends.network.pipeline.out.PipelineOutBuilder;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collections;
import java.util.concurrent.Executors;

public class PipeslineIO {
    public static PipelineOut buildPipelinesOut(AsynchronousSocketChannel client) {
        return new PipelineOutBuilder()
                .client(client)
                .allocateDirect(true)
                .initBuffer(2048)
                .build();
    }

    public static void buildPipelinesIn(AsynchronousSocketChannel client) {
        PipelineOut pipelineOut = buildPipelinesOut(client);
        Callback responseCallback = new CallbackBuilder()
                .onComplete(o -> Executors.newSingleThreadScheduledExecutor().execute(() -> {

                    System.out.println(o);
                    pipelineOut.registerRequest(new SayHelloToClient("Message from Embedded Server: Hi Client!"));
                }))
                .onException(Throwable::printStackTrace)
                .build();

        new PipelineInBuilder()
                .setInitSize(4096*2)
                .setMaxSize(4096*4)
                .configureAggregateCallback(Collections.singletonList(responseCallback))
                .client(client)
                .build();

    }
}
