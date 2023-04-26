package net.techtrends.general.listeners.input;

import net.techtrends.client.AsyncSocket;
import net.techtrends.general.Listener;
import net.techtrends.general.listeners.ResponseCallback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * The InputListener class is responsible for handling input events and processing
 * data received through an AsynchronousSocketChannel. It implements the InputEventHandler
 * interface to provide a way to process various data types such as strings, integers,floats, doubles, and characters.
 * This class maintains a queue of ByteBuffers for incoming data and provides  methods to handle incoming data based on their types.
 * It also allows for the option to allocate resources directly if needed.
 */
public class InputListener implements InputEventHandler {
    private final AsynchronousSocketChannel socketChannel;
    private final ConcurrentLinkedQueue<ByteBuffer> inputQueue = new ConcurrentLinkedQueue<>();

    public InputListener(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    private void handleReadCompletion(ByteBuffer readBuffer, CompletionHandler<Integer, Object> completionHandler) {
        readBuffer.clear();
        inputQueue.add(readBuffer);
        socketChannel.read(readBuffer, null, completionHandler);
    }

    private void handleString(ByteBuffer readBuffer, ResponseCallback callback, CompletionHandler<Integer, Object> completionHandler) {
        CompletableFuture.runAsync(() -> callback.complete(new String(readBuffer.array(), StandardCharsets.UTF_8)))
                .whenComplete((unused, throwable) -> handleReadCompletion(readBuffer, completionHandler));
    }

    private void handleInt(ByteBuffer readBuffer, ResponseCallback callback, CompletionHandler<Integer, Object> completionHandler) {
        CompletableFuture.runAsync(() -> callback.complete(readBuffer.getInt()))
                .whenComplete((unused, throwable) -> handleReadCompletion(readBuffer, completionHandler));
    }

    private void handleFloat(ByteBuffer readBuffer, ResponseCallback callback, CompletionHandler<Integer, Object> completionHandler) {
        CompletableFuture.runAsync(() -> callback.complete(readBuffer.getFloat()))
                .whenComplete((unused, throwable) -> handleReadCompletion(readBuffer, completionHandler));
    }

    private void handleDouble(ByteBuffer readBuffer, ResponseCallback callback, CompletionHandler<Integer, Object> completionHandler) {
        CompletableFuture.runAsync(() -> callback.complete(readBuffer.getDouble()))
                .whenComplete((unused, throwable) -> handleReadCompletion(readBuffer, completionHandler));
    }

    private void handleChar(ByteBuffer readBuffer, ResponseCallback callback, CompletionHandler<Integer, Object> completionHandler) {
        CompletableFuture.runAsync(() -> callback.complete(readBuffer.getChar()))
                .whenComplete((unused, throwable) -> handleReadCompletion(readBuffer, completionHandler));
    }


    @Override
    public void handle(boolean allocateDirect, ResponseCallback callback) {
        Listener.getInstance().getExecutors().execute(() -> {
            ByteBuffer readBuffer;

            if (allocateDirect) {
                inputQueue.add(ByteBuffer.allocateDirect(1024));
            } else {
                inputQueue.add(ByteBuffer.allocate(1024));
            }
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
                            case 0x01 -> handleString(readBuffer, callback, this);
                            case 0x02 -> handleInt(readBuffer, callback, this);
                            case 0x03 -> handleFloat(readBuffer, callback, this);
                            case 0x04 -> handleDouble(readBuffer, callback, this);
                            case 0x05 -> handleChar(readBuffer, callback, this);
                            default -> AsyncSocket.closeSocketChannel(socketChannel);
                        }
                    }

                    @Override
                    public void failed(Throwable exc, Object attachment) {
                        System.err.println("Error while sending data: " + exc.getMessage());
                        AsyncSocket.closeSocketChannel(socketChannel);
                    }
                });
            }
        });
    }
}
