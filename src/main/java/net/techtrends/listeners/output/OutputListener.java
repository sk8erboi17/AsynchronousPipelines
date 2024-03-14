package net.techtrends.listeners.output;

import net.techtrends.exception.MaxBufferSizeExceededException;
import net.techtrends.listeners.response.Callback;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class OutputListener implements CompletionHandler<Integer, ByteBuffer> {
    private final AsynchronousSocketChannel socketChannel;
    private final ByteBuffer outputBuffer;

    public OutputListener(AsynchronousSocketChannel socketChannel, int initialBufferSize, boolean allocateDirect) {
        this.socketChannel = socketChannel;
        if (allocateDirect) {
            this.outputBuffer = ByteBuffer.allocateDirect(initialBufferSize);
        } else {
            this.outputBuffer = ByteBuffer.allocate(initialBufferSize);
        }
    }

    public void sendInt(int data, Callback callback) {
        byte marker = 0x02;
        int dataSize = Integer.BYTES;

        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException();
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }

        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putInt(data);
        outputBuffer.flip();

        performSend(callback);
    }

    public void sendString(String data, Callback callback) {
        byte marker = 0x01;
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        int dataSize = bytes.length + 1;

        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException();
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }

        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.put(bytes);
        outputBuffer.flip();

        performSend(callback);
    }

    public void sendFloat(float data, Callback callback) {
        byte marker = 0x03;
        int dataSize = Float.BYTES;

        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException();
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }

        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putFloat(data);
        outputBuffer.flip();

        performSend(callback);
    }

    public void sendDouble(double data, Callback callback) {
        byte marker = 0x04;
        int dataSize = Double.BYTES;

        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException();
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }

        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putDouble(data);
        outputBuffer.flip();

        performSend(callback);
    }

    public void sendChar(char data, Callback callback) {
        byte marker = 0x05;
        int dataSize = Character.BYTES;
        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException();
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }

        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.putChar(data);
        outputBuffer.flip();

        performSend(callback);
    }

    public void sendByteArray(byte[] data, Callback callback) {
        byte marker = 0x06;
        int dataSize = data.length + 1;

        if (dataSize > outputBuffer.capacity()) {
            try {
                throw new MaxBufferSizeExceededException();
            } catch (MaxBufferSizeExceededException e) {
                throw new RuntimeException(e);
            }
        }
        outputBuffer.clear();
        outputBuffer.put(marker);
        outputBuffer.put(data);
        outputBuffer.flip();

        performSend(callback);
    }

    private void writeOutputBuffer() {
        CompletableFuture.runAsync(() -> socketChannel.write(outputBuffer, outputBuffer, this));
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
        AsyncChannelSocket.closeChannelSocketChannel(socketChannel);
    }

    private void performSend(Callback callback) {
        if (!socketChannel.isOpen()) {
            AsyncChannelSocket.closeChannelSocketChannel(socketChannel);
            callback.complete(false);
            return;
        }

        writeOutputBuffer();
    }


}