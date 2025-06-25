package it.sk8erboi17.network.pipeline.out;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * The PipelineOutBuilder class constructs instances of PipelineOut, which handles the output pipeline for sending data to a client.
 * It provides a flexible configuration for creating PipelineOut objects, allowing options for buffer allocation and size.
 */
public class PipelineOutBuilder {

    private final AsynchronousSocketChannel client;
    private boolean allocateDirect = false;
    private boolean performResizing = false;

    private int bufferSize = 1024;

    public PipelineOutBuilder(AsynchronousSocketChannel client) {
        this.client = client;
    }

    public PipelineOutBuilder allocateDirect(boolean allocateDirect) {
        this.allocateDirect = allocateDirect;
        return this;
    }

    public PipelineOutBuilder setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public PipelineOutBuilder setPerformResizing(boolean performResizing) {
        this.performResizing = performResizing;
        return this;
    }

    public PipelineOut buildSocket() {
        return new PipelineOut(client, allocateDirect, bufferSize,performResizing);
    }

}
