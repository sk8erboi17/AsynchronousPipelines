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
    public static void buildPipelinesOut(AsynchronousSocketChannel client) {
        PipelineOut pipelineOut = new PipelineOutBuilder()
                .client(client)
                .allocateDirect(true)
                .initBuffer(2048).build();

        pipelineOut.registerRequest(new SayHelloToClient("Message from server: Hi Client!"));
    }

    public static void buildPipelinesIn(AsynchronousSocketChannel client) {
        Callback responseCallback = new CallbackBuilder()
                .onComplete(System.out::println)
                .onException(Throwable::printStackTrace)
                .build();

        new PipelineInBuilder()
                .configureAggregateCallback(Collections.singletonList(responseCallback))
                .client(client)
                .build();

    }
}
