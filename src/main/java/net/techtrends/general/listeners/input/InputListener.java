package net.techtrends.general.listeners.input;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.techtrends.general.Listener;
import net.techtrends.general.listeners.ResponseCallback;


/*
 *
 * This is a concrete implementation of the InputEventHandler interface.
 * It defines methods for handling different types of input data and reading from a ByteBuffer.
 * The handle() method reads data from the socket channel and invokes the appropriate data handling method based on
 * the data type marker byte read from the ByteBuffer.
 * @param <T> the type of data to be read from the socket channel and passed back to the calling code
 */
public class InputListener implements InputEventHandler {
    private final ConcurrentLinkedQueue<ByteBuffer> inputQueue = new ConcurrentLinkedQueue<>();

    private void handleString(ByteBuffer readBuffer, ResponseCallback callback, AsynchronousSocketChannel socketChannel, CompletionHandler<Integer, Object> completionHandler) {
        CompletableFuture.runAsync(() -> callback.complete(new String(readBuffer.array(), StandardCharsets.UTF_8)))
                .whenComplete((unused, throwable) -> {
                    readBuffer.clear();
                    inputQueue.add(readBuffer);
                    socketChannel.read(readBuffer, null, completionHandler);
                });
    }

    private void handleInt(ByteBuffer readBuffer, ResponseCallback callback, AsynchronousSocketChannel socketChannel, CompletionHandler<Integer, Object> completionHandler) {
        CompletableFuture.runAsync(() -> callback.complete(readBuffer.getInt()))
                .whenComplete((unused, throwable) -> {
                    readBuffer.clear();
                    inputQueue.add(readBuffer);
                    socketChannel.read(readBuffer, null, completionHandler);
                });

    }

    private void handleFloat(ByteBuffer readBuffer, ResponseCallback callback, AsynchronousSocketChannel socketChannel, CompletionHandler<Integer, Object> completionHandler) {
        CompletableFuture.runAsync(() -> callback.complete(readBuffer.getFloat()))
                .whenComplete((unused, throwable) -> {
                    readBuffer.clear();
                    inputQueue.add(readBuffer);
                    socketChannel.read(readBuffer, null, completionHandler);
                });
    }

    private void handleDouble(ByteBuffer readBuffer, ResponseCallback callback, AsynchronousSocketChannel socketChannel, CompletionHandler<Integer, Object> completionHandler) {
        CompletableFuture.runAsync(() -> callback.complete(readBuffer.getDouble()))
                .whenComplete((unused, throwable) -> {
                    readBuffer.clear();
                    inputQueue.add(readBuffer);
                    socketChannel.read(readBuffer, null, completionHandler);
                });
    }

    private void handleChar(ByteBuffer readBuffer, ResponseCallback callback, AsynchronousSocketChannel socketChannel, CompletionHandler<Integer, Object> completionHandler) {
        CompletableFuture.runAsync(() -> callback.complete(readBuffer.getChar()))
                .whenComplete((unused, throwable) -> {
                    readBuffer.clear();
                    inputQueue.add(readBuffer);
                    socketChannel.read(readBuffer, null, completionHandler);
                });
    }

    private void handleDefault(AsynchronousSocketChannel socketChannel) {
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handle(AsynchronousSocketChannel socketChannel, ResponseCallback callback) {

        Listener.getInstance().getExecutors().execute(() -> {
            ByteBuffer readBuffer;
            inputQueue.add(ByteBuffer.allocate(1024));
            while ((readBuffer = inputQueue.poll()) != null) {

                ByteBuffer finalReadBuffer = readBuffer;
                socketChannel.read(readBuffer, null, new CompletionHandler<>() {
                    @Override
                    public void completed(Integer result, Object attachment) {
                        finalReadBuffer.flip();
                        byte[] bytes = finalReadBuffer.array();
                        ByteBuffer readBuffer = ByteBuffer.wrap(bytes);
                        byte marker = readBuffer.get();

                        switch (marker) {
                            case 0x01 -> handleString(readBuffer, callback, socketChannel, this);
                            case 0x02 -> handleInt(readBuffer, callback, socketChannel, this);
                            case 0x03 -> handleFloat(readBuffer, callback, socketChannel, this);
                            case 0x04 -> handleDouble(readBuffer, callback, socketChannel, this);
                            case 0x05 -> handleChar(readBuffer, callback, socketChannel, this);
                            default -> handleDefault(socketChannel);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        try {
                            socketChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }
}
