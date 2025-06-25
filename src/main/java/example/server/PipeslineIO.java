package example.server;

import example.server.request.SayHelloToClient;
import it.sk8erboi17.listeners.response.Callback;
import it.sk8erboi17.listeners.response.CallbackBuilder;
import it.sk8erboi17.network.pipeline.in.PipelineInBuilder;
import it.sk8erboi17.network.pipeline.out.PipelineOut;
import it.sk8erboi17.network.pipeline.out.PipelineOutBuilder;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Collections;

public class PipeslineIO {
    public static void buildPipelinesIn(AsynchronousSocketChannel client) {
        // Create an output pipeline for sending responses back to the client
        PipelineOut pipelineOut = buildPipelinesOut(client);

        // Define a callback to handle responses and manage exceptions
        Callback responseCallback = new CallbackBuilder()
                .onComplete(o -> {
                    System.out.println(o); // Print the response received from the client
                    // Send a response back to the client
                    pipelineOut.handleRequest(new SayHelloToClient("Message from Embedded Server: Hi Client!"));
                })
                .onException(Throwable::printStackTrace) // Print stack trace for exceptions
                .build();

        // Configure and initialize the input pipeline
        new PipelineInBuilder(client)
                .setBufferSize(4096 * 128) // Set the buffer size for receiving data
                .configureAggregateCallback(Collections.singletonList(responseCallback)) // Set up callbacks for handling data and exceptions
                .build(); // Build and initialize the input pipeline
    }

    private static PipelineOut buildPipelinesOut(AsynchronousSocketChannel client) {
        // Configure and create an output pipeline
        return new PipelineOutBuilder(client)
                .allocateDirect(true) // Use direct memory for the buffer
                .setBufferSize(4096 * 20) // Set the buffer size for sending data
                .buildSocket(); // Build and initialize the output pipeline
    }

}
