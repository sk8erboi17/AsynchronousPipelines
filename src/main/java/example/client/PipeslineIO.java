package example.client;


import example.client.request.SayHelloToEmbeddedServer;
import example.client.response.ResponseManager;
import net.techtrends.network.pipeline.in.PipelineInBuilder;
import net.techtrends.network.pipeline.out.PipelineOut;
import net.techtrends.network.pipeline.out.PipelineOutBuilder;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class PipeslineIO {

    ///EMBEDDED SERVER REQUEST HERE
    public static void buildPipelinesSocketOut(AsynchronousSocketChannel client) {
        PipelineOut pipelineOut = new PipelineOutBuilder(client).allocateDirect(true).setBufferSize(4096).buildSocket();
        pipelineOut.handleRequest(new SayHelloToEmbeddedServer("Message from Client: Hi Embedded Server!"));

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            pipelineOut.handleRequest(new SayHelloToEmbeddedServer("Message from Client: Hi Embedded Server!"));
        }, 1, 1, TimeUnit.NANOSECONDS);

    }


    public static void buildPipelinesIn(AsynchronousSocketChannel client) {
        new PipelineInBuilder(client)
                .setBufferSize(4096)
                .configureAggregateCallback(Collections.singletonList(ResponseManager.responseToServer))
                .build();
    }


}
