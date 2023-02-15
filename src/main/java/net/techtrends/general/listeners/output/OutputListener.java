package net.techtrends.general.listeners.output;

import net.techtrends.general.Listener;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

/*
 *
 * This is a concrete implementation of the OututEventHandler interface.
 * It defines methods for handling different types of output data and writing from a ByteBuffer.
 * The handle() method write data from the socket channel and invokes the appropriate data handling method based on
 * the data type marker byte read from the ByteBuffer.
 * @param <T> the type of data to be write from the socket channel and passed back to the calling code
 */

public class OutputListener implements OutputEventHandler {
    private final ConcurrentLinkedQueue<ByteBuffer> outputQueue = new ConcurrentLinkedQueue<>();

    private void sendString(String data) {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        buffer.put((byte) 0x01);
        byte[] bytes = data.getBytes(StandardCharsets.UTF_8);
        buffer.put(bytes);
        outputQueue.add(buffer);
    }

    private void sendInt(Integer data) {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        buffer.put((byte) 0x02);
        byte[] bytes = ByteBuffer.allocate(Integer.BYTES).putInt(data).array();
        buffer.put(bytes);
        outputQueue.add(buffer);
    }

    private void sendFloat(Float data) {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        buffer.put((byte) 0x03);
        byte[] bytes = ByteBuffer.allocate(Float.BYTES).putFloat(data).array();
        buffer.put(bytes);
        outputQueue.add(buffer);
    }

    private void sendDouble(Double data) {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        buffer.put((byte) 0x04);
        byte[] bytes = ByteBuffer.allocate(Double.BYTES).putDouble(data).array();
        buffer.put(bytes);
        outputQueue.add(buffer);
    }

    private void sendChar(Character data) {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        buffer.put((byte) 0x05);
        byte[] bytes = ByteBuffer.allocate(Character.BYTES).putChar(data).array();
        buffer.put(bytes);
        outputQueue.add(buffer);
    }


    private void sendOutput(AsynchronousSocketChannel socketChannel) {
        ByteBuffer buffer;
        while ((buffer = outputQueue.poll()) != null) {
            buffer.flip();
            try {
                socketChannel.write(buffer).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
    }
    @Override
    public void handle(AsynchronousSocketChannel socketChannel,Object data) {
        Listener.getInstance().getExecutors().execute(() -> {
        if (data == null) {
            throw new IllegalArgumentException("Data cannot be null.");
        }
        if (!socketChannel.isOpen()) {
            throw new IllegalStateException("Socket channel is closed.");
        }
        if (data instanceof String) {
            sendString((String) data);
        } else if (data instanceof Integer) {
            sendInt((Integer) data);
        } else if (data instanceof Float) {
            sendFloat((Float) data);
        } else if (data instanceof Double) {
            sendDouble((Double) data);
        } else if (data instanceof Character) {
            sendChar((Character) data);
        } else {
            throw new IllegalArgumentException("Invalid data type: " + data.getClass().getName());
        }
       sendOutput(socketChannel);
    });
}

}
