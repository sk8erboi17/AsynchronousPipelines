package net.techtrends.listeners.input;

import net.techtrends.BufferBuilder;
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
    private final boolean allocateDirect;
    private final int initialBufferSize;

    private final Callback callback;

    public InputListener(AsynchronousSocketChannel socketChannel, boolean allocateDirect, int initialBufferSize, int maxBufferSize, Callback callback) {
        this.socketChannel = socketChannel;
        this.callback = callback;
        this.maxBufferSize = maxBufferSize;
        this.allocateDirect = allocateDirect;
        this.initialBufferSize = initialBufferSize;
    }

    @Override
    public void completed(Integer bytesRead, ByteBuffer buffer) {
        if (bytesRead < 0) {
            AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
            return;
        }
        if (bytesRead > 0) {
            if (buffer.remaining() > maxBufferSize && buffer.capacity() > maxBufferSize) {
                AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
                try {
                    throw new MaxBufferSizeExceededException();
                } catch (MaxBufferSizeExceededException e) {
                    e.printStackTrace();
                }
                return;
            }
            buffer.flip();
            send(buffer);
            buffer.clear();
        }

        readThread.execute(() -> startRead(buffer));
    }


    private void send(ByteBuffer buffer) {
        ListenData processData = new ListenData();
        processData.listen(buffer, callback);
    }

    public void startRead(ByteBuffer buffer){
        socketChannel.read(buffer,buffer,this);
    }

    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
    }

    public void close() {
        AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
    }
}
