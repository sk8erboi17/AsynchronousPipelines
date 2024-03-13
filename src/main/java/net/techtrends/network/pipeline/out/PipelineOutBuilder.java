package net.techtrends.network.pipeline.out;

import java.nio.channels.AsynchronousSocketChannel;

public class PipelineOutBuilder {

    private AsynchronousSocketChannel client;
    private boolean allocateDirect = false;
    private int initBuffer = 1024;
    private boolean isHttpEnabled = false;

    public PipelineOutBuilder client(AsynchronousSocketChannel client) {
        this.client = client;
        return this;
    }

    public PipelineOutBuilder setHttpEnabled(boolean httpEnabled) {
        this.isHttpEnabled = httpEnabled;
        return this;
    }


    public PipelineOutBuilder allocateDirect(boolean allocateDirect) {
        this.allocateDirect = allocateDirect;
        return this;
    }

    public PipelineOutBuilder initBuffer(int initBuffer) {
        this.initBuffer = initBuffer;
        return this;
    }

    public PipelineOut build() {
        return new PipelineOut(client, allocateDirect, initBuffer, isHttpEnabled);
    }
}
