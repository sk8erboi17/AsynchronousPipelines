package net.techtrends.listeners.input;

import net.techtrends.exception.MaxBufferSizeExceededException;
import net.techtrends.listeners.input.operations.ListenData;
import net.techtrends.listeners.output.AsyncOutputSocket;
import net.techtrends.listeners.response.Callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InputListener implements CompletionHandler<Integer, ByteBuffer> {
    private final ExecutorService readThread = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() / 2);
    private final AsynchronousSocketChannel socketChannel;
    private final int maxBufferSize;
    private final Callback callback;
    private ByteBuffer inputBuffer;

    public InputListener(AsynchronousSocketChannel socketChannel, boolean allocateDirect, int initialBufferSize, int maxBufferSize, Callback callback) {
        this.socketChannel = socketChannel;
        this.callback = callback;
        this.maxBufferSize = maxBufferSize;
        if (allocateDirect) {
            this.inputBuffer = ByteBuffer.allocateDirect(initialBufferSize);
        } else {
            this.inputBuffer = ByteBuffer.allocate(initialBufferSize);
        }
    }


    public void start() {
        socketChannel.read(inputBuffer, inputBuffer, this);
    }

    @Override
    public void completed(Integer bytesRead, ByteBuffer buffer) {
        if (bytesRead < 0) {
            AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
            return;
        }
        if (bytesRead > 0) {

            if (buffer.remaining() < maxBufferSize && buffer.capacity() < maxBufferSize) {
                double bufferUsagePercentage = (double) buffer.position() / buffer.capacity();
                if (bufferUsagePercentage >= 0.75) {

                    ByteBuffer newBuffer = buffer.isDirect() ?
                            ByteBuffer.allocateDirect(Math.min(buffer.capacity() * 2, maxBufferSize)) :
                            ByteBuffer.allocate(Math.min(buffer.capacity() * 2, maxBufferSize));

                    buffer.flip();
                    newBuffer.put(buffer);
                    inputBuffer = newBuffer;
                }
            } else {
                AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
                try {
                    throw new MaxBufferSizeExceededException();
                } catch (MaxBufferSizeExceededException e) {
                    e.printStackTrace();
                }
            }


            buffer.flip();
            send(buffer);
            buffer.clear();
            inputBuffer.clear();
        }

        readThread.execute(() -> socketChannel.read(inputBuffer, inputBuffer, this));
    }


    private void send(ByteBuffer buffer){
        ListenData processData = new ListenData();
        processData.listen(buffer,callback);
    }
    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
    }

    public void close() {
        AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
    }
}
