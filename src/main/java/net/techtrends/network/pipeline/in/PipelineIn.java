package net.techtrends.network.pipeline.in;

import net.techtrends.BufferBuilder;
import net.techtrends.listeners.input.InputListener;
import net.techtrends.listeners.response.Callback;
import net.techtrends.network.pipeline.Pipeline;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

public class PipelineIn implements Pipeline {
    private static InputListener inputListener = null;

    public PipelineIn(AsynchronousSocketChannel client, boolean allocateDirect, int bufferSize, Callback callback) {
        inputListener = new InputListener(client,  bufferSize, callback);
        inputListener.startRead(new BufferBuilder().setInitSize(bufferSize).allocateDirect(allocateDirect).build());
    }

    @Override
    public void closePipeline() {
        inputListener.close();
    }

}
