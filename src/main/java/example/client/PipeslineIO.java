package example.client;


import example.client.request.GetUsersFromWebServer;
import example.client.request.SayHelloToEmbeededServer;
import example.client.response.ResponseManager;
import net.techtrends.network.pipeline.Pipeline;
import net.techtrends.network.pipeline.in.PipelineIn;
import net.techtrends.network.pipeline.in.PipelineInBuilder;
import net.techtrends.network.pipeline.out.PipelineOut;
import net.techtrends.network.pipeline.out.PipelineOutBuilder;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collections;
import java.util.concurrent.Executors;


public class PipeslineIO {

    //HTTP REQUEST HERE
    public static void buildPipelinesHttpOut(AsynchronousSocketChannel client) {
        PipelineOut pipelineOut = new PipelineOutBuilder().client(client).allocateDirect(true).setHttpEnabled(true).setBufferSize(4096).build();
        pipelineOut.registerRequest(new GetUsersFromWebServer().request());
        closePipeline(pipelineOut);
    }

    ///EMBEEDSERVER REQUEST HERE
    public static void buildPipelinesSocketOut(AsynchronousSocketChannel client) {
        PipelineOut pipelineOut = new PipelineOutBuilder().client(client).allocateDirect(true).setBufferSize(4096).setHttpEnabled(false).build();
        Executors.newSingleThreadExecutor().execute(() -> {
            while (true) {
                pipelineOut.registerRequest(new SayHelloToEmbeededServer("Message from Client: Hi Embedded Server!"));
                try {
                    Thread.sleep(105);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

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
