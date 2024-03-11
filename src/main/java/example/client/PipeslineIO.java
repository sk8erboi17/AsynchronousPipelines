package example.client;


import example.client.request.SayHelloToServer;
import example.client.response.ResponseManager;
import net.techtrends.network.pipeline.Pipeline;
import net.techtrends.network.pipeline.in.PipelineIn;
import net.techtrends.network.pipeline.in.PipelineInBuilder;
import net.techtrends.network.pipeline.out.PipelineOut;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class PipeslineIO {
    public static void buildPipelinesOut(PipelineOut pipeline) {

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            pipeline.registerRequest(new SayHelloToServer());
        }, 500, 500, TimeUnit.MILLISECONDS);

        closePipeline(pipeline);
    }

    public static void buildPipelinesIn(AsynchronousSocketChannel client) {
        PipelineIn pipelineIn = new PipelineInBuilder()
                .configureAggregateCallback(Arrays.asList(ResponseManager.responseToServer, ResponseManager.secondResponse))
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
