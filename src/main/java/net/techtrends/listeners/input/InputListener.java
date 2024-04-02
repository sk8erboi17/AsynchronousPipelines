package net.techtrends.listeners.input;

import net.techtrends.exception.MaxBufferSizeExceededException;
import net.techtrends.listeners.input.operations.ListenData;
import net.techtrends.listeners.output.AsyncChannelSocket;
import net.techtrends.listeners.response.Callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InputListener implements CompletionHandler<Integer, ByteBuffer> {
    private final ExecutorService readThread;

    private final ListenData processData;

    private final AsynchronousSocketChannel socketChannel;

    private final int bufferSize;

    private final Callback callback;

    public InputListener(AsynchronousSocketChannel socketChannel, int bufferSize, Callback callback) {
        this.socketChannel = socketChannel;
        this.callback = callback;
        this.bufferSize = bufferSize;
        this.readThread = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() / 2);
        this.processData = new ListenData();
    }

    @Override
    public void completed(Integer bytesRead, ByteBuffer buffer) {
        if (bytesRead < 0) {
            AsyncChannelSocket.closeChannelSocketChannel(socketChannel);
            throw new RuntimeException("Not enough byte to read");
        }
        if (buffer.remaining() >= bufferSize) {
            AsyncChannelSocket.closeChannelSocketChannel(socketChannel);
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

        readThread.execute(() -> startRead(buffer));
    }


    private void send(ByteBuffer buffer) {
        processData.listen(buffer, callback);
    }

    public void startRead(ByteBuffer buffer) {
        socketChannel.read(buffer, buffer, this);
    }

    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        System.out.println(exc.getClass());
        AsyncChannelSocket.closeChannelSocketChannel(socketChannel);
        throw new RuntimeException("Error: " + exc.getMessage(), exc);
    }

}
