package example.client;


import example.client.request.GetUsersFromWebServer;
import example.client.request.SayHelloToEmbeddedServer;
import example.client.response.ResponseManager;
import net.techtrends.network.pipeline.in.PipelineInBuilder;
import net.techtrends.network.pipeline.out.PipelineOut;
import net.techtrends.network.pipeline.out.PipelineOutBuilder;
import net.techtrends.network.pipeline.out.content.http.Http;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


public class PipeslineIO {

    //HTTP REQUEST HERE
    public static void buildPipelinesHttpOut() {
        PipelineOut pipelineOut = new PipelineOutBuilder().buildHTTP();

        Http request = new GetUsersFromWebServer();
        pipelineOut.registerRequest(request);

    }

    ///EMBEDDED SERVER REQUEST HERE
    public static void buildPipelinesSocketOut(AsynchronousSocketChannel client) {
        PipelineOut pipelineOut = new PipelineOutBuilder(client).allocateDirect(true).setBufferSize(4096).buildSocket();

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            pipelineOut.registerRequest(new SayHelloToEmbeddedServer("Message from Client: Hi Embedded Server!"));
        }, 1, 1, TimeUnit.NANOSECONDS);

    }


    public static void buildPipelinesIn(AsynchronousSocketChannel client) {
        new PipelineInBuilder(client)
                .setBufferSize(4096)
                .configureAggregateCallback(Collections.singletonList(ResponseManager.responseToServer))
                .build();
    }


}
