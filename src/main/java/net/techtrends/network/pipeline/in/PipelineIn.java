package net.techtrends.network.pipeline.in;

import net.techtrends.BufferBuilder;
import net.techtrends.listeners.input.InputListener;
import net.techtrends.network.AggregateCallback;

import java.nio.channels.AsynchronousSocketChannel;

public class PipelineIn {

    public PipelineIn(AsynchronousSocketChannel client, boolean allocateDirect, int bufferSize, AggregateCallback callback) {
        InputListener inputListener = new InputListener(client, bufferSize, callback);
        inputListener.startRead(new BufferBuilder().setInitSize(bufferSize).allocateDirect(allocateDirect).build());
    }

}
