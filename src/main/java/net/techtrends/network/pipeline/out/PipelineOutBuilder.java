package net.techtrends.network.pipeline.out;

import java.nio.channels.AsynchronousSocketChannel;

public class PipelineOutBuilder {

    private AsynchronousSocketChannel client;
    private boolean allocateDirect = false;
    private int bufferSize = 1024;

    public PipelineOutBuilder(AsynchronousSocketChannel client) {
        this.client = client;
    }

    public PipelineOutBuilder() {
    }


    public PipelineOutBuilder allocateDirect(boolean allocateDirect) {
        this.allocateDirect = allocateDirect;
        return this;
    }

    public PipelineOutBuilder setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
        return this;
    }

    public PipelineOut buildSocket() {
        PipelineOut pipelineOut = new PipelineOut(client, allocateDirect, bufferSize);
        return pipelineOut;
    }

    public PipelineOut buildHTTP() {
        return new PipelineOut();
    }


}
