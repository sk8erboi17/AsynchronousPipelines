package it.sk8erboi17.network.pipeline.out;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * The PipelineOutBuilder class constructs instances of PipelineOut, which handles the output pipeline for sending data to a client.
 * It provides a flexible configuration for creating PipelineOut objects, allowing options for buffer allocation and size.
 */
public class PipelineOutBuilder {

    private final AsynchronousSocketChannel client;

    public PipelineOutBuilder(AsynchronousSocketChannel client) {
        this.client = client;
    }


    public PipelineOut buildSocket() {
        return new PipelineOut(client);
    }

}
