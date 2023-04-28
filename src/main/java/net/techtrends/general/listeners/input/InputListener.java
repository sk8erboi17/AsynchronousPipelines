package net.techtrends.general.listeners.input;

import net.techtrends.client.AsyncSocket;
import net.techtrends.general.listeners.ResponseCallback;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The InputListener class is responsible for handling input events and receiving
 * data through an AsynchronousSocketChannel. It implements the CompletionHandler
 * interface to provide a way to process and receive data from the channel.
 * This class maintains a ByteBuffer for incoming data and provides a method to read
 * the data when it is available.
 */
public class InputListener implements CompletionHandler<Integer, ByteBuffer> {
    private final AsynchronousSocketChannel socketChannel;
    private final ByteBuffer inputBuffer;
    private final ResponseCallback responseCallback;
    private static final int BUFFER_SIZE = 2048;
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);

    public InputListener(AsynchronousSocketChannel socketChannel, boolean allocateDirect, ResponseCallback responseCallback) {
        this.socketChannel = socketChannel;
        this.responseCallback = responseCallback;
        if (allocateDirect) {
            this.inputBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
        } else {
            this.inputBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        }
    }

    public void start() {
        socketChannel.read(inputBuffer, inputBuffer, this);
    }

    @Override
    public void completed(Integer bytesRead, ByteBuffer buffer) {
        if (bytesRead < 0) {
            AsyncSocket.closeSocketChannel(socketChannel);
            return;
        }
        if (bytesRead > 0) {
            buffer.flip();
            byte marker = buffer.get();
            if (marker >= 0x01 && marker <= 0x06) {
                switch (marker) {
                    case 0x01 -> handleString(buffer, responseCallback);
                    case 0x02 -> handleInt(buffer, responseCallback);
                    case 0x03 -> handleFloat(buffer, responseCallback);
                    case 0x04 -> handleDouble(buffer, responseCallback);
                    case 0x05 -> handleChar(buffer, responseCallback);
                    case 0x06 -> handleByteArray(buffer, responseCallback);
                }
            } else {
                AsyncSocket.closeSocketChannel(socketChannel);
                System.out.println("Invalid marker received!");
            }
            buffer.clear();
        }
        socketChannel.read(buffer, buffer, this);
    }

    private void handleString(ByteBuffer buffer, ResponseCallback callback) {
        String data = StandardCharsets.UTF_8.decode(buffer).toString();
        CompletableFuture.runAsync(() -> callback.complete(data), executorService);
    }

    private void handleInt(ByteBuffer buffer, ResponseCallback callback) {
        int data = buffer.getInt();
        CompletableFuture.runAsync(() -> callback.complete(data), executorService);
    }

    private void handleFloat(ByteBuffer buffer, ResponseCallback callback) {
        float data = buffer.getFloat();
        CompletableFuture.runAsync(() -> callback.complete(data), executorService);
    }

    private void handleDouble(ByteBuffer buffer, ResponseCallback callback) {
        double data = buffer.getDouble();
        CompletableFuture.runAsync(() -> callback.complete(data), executorService);
    }

    private void handleChar(ByteBuffer buffer, ResponseCallback callback) {
        char data = buffer.getChar();
        CompletableFuture.runAsync(() -> callback.complete(data), executorService);
    }

    private void handleByteArray(ByteBuffer buffer, ResponseCallback callback) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        CompletableFuture.runAsync(() -> callback.complete(data), executorService);
    }

    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        AsyncSocket.closeSocketChannel(socketChannel);
        System.out.println("Client disconnect!");
        // Implement additional error handling or logging here as needed
    }


    public void close() {
        AsyncSocket.closeSocketChannel(socketChannel);
        executorService.shutdown(); // Gracefully shut down the executor service
    }
}
