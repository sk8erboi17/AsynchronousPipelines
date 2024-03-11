package net.techtrends.network.pipeline.in;

import net.techtrends.listeners.response.Callback;
import net.techtrends.network.AggregateCallback;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.List;

public class PipelineInBuilder {

    private AsynchronousSocketChannel client;
    private boolean allocateDirect = false;
    private int initBuffer = 1024;
    private int maxBuffer = 4096;
    private AggregateCallback aggregateCallback;

    public PipelineInBuilder configureAggregateCallback(List<Callback> callbacks) {
        this.aggregateCallback = new AggregateCallback(callbacks);
        return this;
    }

    public PipelineInBuilder client(AsynchronousSocketChannel client) {
        this.client = client;
        return this;
    }

    public PipelineInBuilder allocateDirect(boolean allocateDirect) {
        this.allocateDirect = allocateDirect;
        return this;
    }

    public PipelineInBuilder initBuffer(int initBuffer) {
        this.initBuffer = initBuffer;
        return this;
    }

    public PipelineInBuilder maxBuffer(int maxBuffer) {
        this.maxBuffer = maxBuffer;
        return this;
    }

    public PipelineIn build() {
        return new PipelineIn(client, allocateDirect, initBuffer, maxBuffer, aggregateCallback);
    }
}
