package net.techtrends.listeners.output;

import net.techtrends.listeners.response.Callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class OutputListener implements CompletionHandler<Integer, ByteBuffer> {
    private static AsynchronousSocketChannel socketChannel;
    private ByteBuffer outputBuffer;

    public OutputListener(AsynchronousSocketChannel socketChannel, int initialBufferSize, boolean allocateDirect) {
        OutputListener.socketChannel = socketChannel;

        if (allocateDirect) {
            this.outputBuffer = ByteBuffer.allocateDirect(initialBufferSize);
        } else {
            this.outputBuffer = ByteBuffer.allocate(initialBufferSize);
        }
    }

    public void sendInt(int data, Callback callback) {
        byte marker = 0x02;
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putInt(data);
        outputBuffer.flip();

        if (socketChannel.isOpen()) {
            CompletableFuture.supplyAsync(this::writeOutputBuffer).whenComplete((result, throwable) -> {
                if (throwable != null || !result) {
                    callback.completeExceptionally(throwable);
                    AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
                } else {
                    callback.complete(true);
                }
            });
        } else {
            AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
        }
    }

    public void sendString(String data, Callback callback) {
        byte marker = 0x01;
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        int dataSize = bytes.length + 1;
        if (dataSize > outputBuffer.capacity()) {
            outputBuffer = expandByteBuffer(dataSize);
        }
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.put(bytes);
        outputBuffer.put((byte) 0);
        outputBuffer.flip();

        if (socketChannel.isOpen()) {
            CompletableFuture.supplyAsync(this::writeOutputBuffer).whenComplete((result, throwable) -> {
                if (throwable != null || !result) {
                    callback.completeExceptionally(throwable);
                    AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
                } else {
                    callback.complete(true);
                }
            });
        } else {
            AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
        }
    }

    public void sendFloat(float data, Callback callback) {
        byte marker = 0x03;
        int dataSize = Float.BYTES;
        if (dataSize > outputBuffer.capacity()) {
            outputBuffer = expandByteBuffer(dataSize);
        }
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putFloat(data);
        outputBuffer.flip();

        if (socketChannel.isOpen()) {
            CompletableFuture.supplyAsync(this::writeOutputBuffer).whenComplete((result, throwable) -> {
                if (throwable != null || !result) {
                    callback.completeExceptionally(throwable);
                    AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
                } else {
                    callback.complete(true);
                }
            });
        } else {
            AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
        }
    }

    public void sendDouble(double data, Callback callback) {
        byte marker = 0x04;
        int dataSize = Double.BYTES;
        if (dataSize > outputBuffer.capacity()) {
            outputBuffer = expandByteBuffer(dataSize);
        }
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putDouble(data);
        outputBuffer.flip();

        if (socketChannel.isOpen()) {
            CompletableFuture.supplyAsync(this::writeOutputBuffer).whenComplete((result, throwable) -> {
                if (throwable != null || !result) {
                    callback.completeExceptionally(throwable);
                    AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
                } else {
                    callback.complete(true);
                }
            });
        } else {
            AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
        }
    }

    public void sendChar(char data, Callback callback) {
        byte marker = 0x05;
        int dataSize = Character.BYTES;
        if (dataSize > outputBuffer.capacity()) {
            outputBuffer = expandByteBuffer(dataSize);
        }
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putChar(data);
        outputBuffer.flip();
        if (socketChannel.isOpen()) {
            CompletableFuture.supplyAsync(this::writeOutputBuffer).whenComplete((result, throwable) -> {
                if (throwable != null || !result) {
                    callback.completeExceptionally(throwable);
                    AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
                } else {
                    callback.complete(true);
                }
            });
        } else {
            AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
        }
    }

    public void sendByteArray(byte[] data, Callback callback) {
        byte marker = 0x06;
        int dataSize = data.length + 1;
        if (dataSize > outputBuffer.capacity()) {
            outputBuffer = expandByteBuffer(dataSize);
        }
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.put(data);
        outputBuffer.flip();

        if (socketChannel.isOpen()) {
            CompletableFuture.supplyAsync(this::writeOutputBuffer).whenComplete((result, throwable) -> {
                if (throwable != null || !result) {
                    callback.completeExceptionally(throwable);
                    AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
                } else {
                    callback.complete(true);
                }
            });
        } else {
            AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
        }
    }

    public void sendStringSanitized(String data, Callback callback) {
        byte marker = 0x07;
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        int dataSize = bytes.length + 1;
        if (dataSize > outputBuffer.capacity()) {
            outputBuffer = expandByteBuffer(dataSize);
        }
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.put(bytes);
        outputBuffer.put((byte) 0);
        outputBuffer.flip();

        if (socketChannel.isOpen()) {
            CompletableFuture.supplyAsync(this::writeOutputBuffer).whenComplete((result, throwable) -> {
                if (throwable != null || !result) {
                    callback.completeExceptionally(throwable);
                    AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
                } else {
                    callback.complete(true);
                }
            });
        } else {
            AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
        }
    }

    private ByteBuffer expandByteBuffer(int dataSize) {
        int newBufferSize = Math.max(outputBuffer.capacity() * 2, dataSize);
        ByteBuffer newBuffer;
        if (outputBuffer.isDirect()) {
            newBuffer = ByteBuffer.allocateDirect(newBufferSize);
        } else {
            newBuffer = ByteBuffer.allocate(newBufferSize);
        }
        outputBuffer.flip();
        newBuffer.put(outputBuffer);
        return newBuffer;
    }

    private boolean writeOutputBuffer() {
        try {
            socketChannel.write(outputBuffer).get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    @Override
    public void completed(Integer bytesWritten, ByteBuffer buffer) {
        if (bytesWritten < 0) {
            return;
        }
        if (buffer.hasRemaining()) {
            socketChannel.write(buffer, buffer, this);
        }
    }

    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        System.err.println("Error while sending data: " + exc.getMessage());
        exc.printStackTrace();
        AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
    }

    public void close() {
        AsyncOutputSocket.closeOutputSocketChannel(socketChannel);
    }
}
