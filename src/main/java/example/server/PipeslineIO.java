package example.server;

import example.server.request.SayHelloToClient;
import net.techtrends.listeners.response.Callback;
import net.techtrends.listeners.response.CallbackBuilder;
import net.techtrends.network.pipeline.in.PipelineInBuilder;
import net.techtrends.network.pipeline.out.PipelineOut;
import net.techtrends.network.pipeline.out.PipelineOutBuilder;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collections;

public class PipeslineIO {
    public static PipelineOut buildPipelinesOut(AsynchronousSocketChannel client) {
        return new PipelineOutBuilder(client)
                .allocateDirect(true)
                .setBufferSize(4096 * 20)
                .buildSocket();
    }

    public static void buildPipelinesIn(AsynchronousSocketChannel client) {
        PipelineOut pipelineOut = buildPipelinesOut(client);

        Callback responseCallback = new CallbackBuilder()
                .onComplete(o -> {
                    System.out.println(o);
                    pipelineOut.registerRequest(new SayHelloToClient("Message from Embedded Server: Hi Client!"));
                })
                .onException(Throwable::printStackTrace)
                .build();

        new PipelineInBuilder(client)
                .setBufferSize(4096 * 128)
                .configureAggregateCallback(Collections.singletonList(responseCallback))
                .build();

    }
}
