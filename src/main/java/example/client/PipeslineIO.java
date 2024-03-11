package example.client;


import example.client.request.GetUsersFromWebServer;
import example.client.request.SayHelloToEmbeededServer;
import example.client.response.ResponseManager;
import net.techtrends.network.pipeline.Pipeline;
import net.techtrends.network.pipeline.in.PipelineIn;
import net.techtrends.network.pipeline.in.PipelineInBuilder;
import net.techtrends.network.pipeline.out.PipelineOut;
import net.techtrends.network.pipeline.out.PipelineOutBuilder;
import net.techtrends.network.pipeline.out.content.http.HttpBuilder;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collections;


public class PipeslineIO {

    //HTTP REQUEST HERE
    public static void buildPipelinesHttpOut(AsynchronousSocketChannel client) {
        PipelineOut pipelineOut = new PipelineOutBuilder().client(client).allocateDirect(true).setHttpEnabled(true).initBuffer(4096).build();
        pipelineOut.registerRequest(new GetUsersFromWebServer().request());
        closePipeline(pipelineOut);
    }

    ///EMBEEDSERVER REQUEST HERE
    public static void buildPipelinesSocketOut(AsynchronousSocketChannel client) {
        PipelineOut pipelineOut = new PipelineOutBuilder().client(client).allocateDirect(true).initBuffer(4096).setHttpEnabled(false).build();
        pipelineOut.registerRequest(new SayHelloToEmbeededServer("Message from Client: Hi Embedded Server!"));
        closePipeline(pipelineOut);
    }


    public static void buildPipelinesIn(AsynchronousSocketChannel client) {
        PipelineIn pipelineIn = new PipelineInBuilder()
                .configureAggregateCallback(Collections.singletonList(ResponseManager.responseToServer))
                .client(client)
                .build();
        closePipeline(pipelineIn);
    }


    private static void closePipeline(Pipeline pipeline) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutdown");
            pipeline.closePipeline();
        }));
    }
}
