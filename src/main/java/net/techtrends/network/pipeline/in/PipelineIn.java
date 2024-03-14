package net.techtrends.network.pipeline.in;

import net.techtrends.BufferBuilder;
import net.techtrends.listeners.input.InputListener;
import net.techtrends.listeners.response.Callback;

import java.nio.channels.AsynchronousSocketChannel;

public class PipelineIn {
    private static InputListener inputListener;

    public PipelineIn(AsynchronousSocketChannel client, boolean allocateDirect, int bufferSize, Callback callback) {
        inputListener = new InputListener(client, bufferSize, callback);
        inputListener.startRead(new BufferBuilder().setInitSize(bufferSize).allocateDirect(allocateDirect).build());
    }

}
