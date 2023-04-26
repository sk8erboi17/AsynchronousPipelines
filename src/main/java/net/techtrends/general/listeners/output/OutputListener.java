package net.techtrends.general.listeners.output;

import net.techtrends.general.Listener;
import net.techtrends.general.listeners.interfaces.PrimitiveWriter;
import net.techtrends.general.listeners.interfaces.SendTask;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The OutputListener class is responsible for handling output events and sending
 * data through an AsynchronousSocketChannel. It implements the OutputEventHandler
 * interface to provide a way to process and send various data types such as
 * strings, integers, floats, doubles, and characters.
 * This class maintains a queue of ByteBuffers for outgoing data and provides
 * methods to send data based on their types. It also allows for the option to allocate resources directly if needed.
 */

public class OutputListener implements OutputEventHandler {
    private final AsynchronousSocketChannel socketChannel;
    private final ConcurrentLinkedQueue<ByteBuffer> outputQueue = new ConcurrentLinkedQueue<>();

    public OutputListener(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }


    private void sendString(String data, boolean allocateDirect) {
        send((byte) 0x01, data.getBytes(StandardCharsets.UTF_8), allocateDirect);
    }

    private void sendInt(Integer data, boolean allocateDirect) throws IOException {
        send((byte) 0x02, toByteArray(dataOutputStream -> dataOutputStream.writeInt(data)), allocateDirect);
    }

    private void sendFloat(Float data, boolean allocateDirect) throws IOException {
        send((byte) 0x03, toByteArray(dataOutputStream -> dataOutputStream.writeFloat(data)), allocateDirect);
    }

    private void sendDouble(Double data, boolean allocateDirect) throws IOException {
        send((byte) 0x04, toByteArray(dataOutputStream -> dataOutputStream.writeDouble(data)), allocateDirect);
    }

    private void sendChar(Character data, boolean allocateDirect) throws IOException {
        send((byte) 0x05, toByteArray(dataOutputStream -> dataOutputStream.writeChar(data)), allocateDirect);
    }

    private void send(byte type, byte[] data, boolean allocateDirect) {
        ByteBuffer buffer = allocateDirect ? ByteBuffer.allocateDirect(2048) : ByteBuffer.allocate(2048);
        buffer.put(type);
        buffer.put(data);
        outputQueue.add(buffer);
    }

    private byte[] toByteArray(PrimitiveWriter writer) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        writer.write(dataOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void sendOutput() {
        ByteBuffer buffer = outputQueue.poll();

        if (buffer != null) {
            buffer.flip();
            socketChannel.write(buffer, buffer, new CompletionHandler<>() {
                @Override
                public void completed(Integer result, ByteBuffer attachment) {
                    if (attachment.hasRemaining()) {
                        socketChannel.write(attachment, attachment, this);
                    } else {
                        sendOutput();
                    }
                }

                @Override
                public void failed(Throwable exc, ByteBuffer attachment) {
                    System.err.println("Error while sending data: " + exc.getMessage());
                    exc.printStackTrace();
                }
            });
        }
    }

    @Override
    public void handle(boolean allocateDirect, Object data) {
        Listener.getInstance().getExecutors().execute(() -> {
            if (data == null) {
                throw new IllegalArgumentException("Data cannot be null.");
            }
            if (!socketChannel.isOpen()) {
                throw new IllegalStateException("Socket channel is closed.");
            }
            if (data instanceof String) {
                sendString((String) data, allocateDirect);
            } else if (data instanceof Integer) {
                handleSend(() -> sendInt((Integer) data, allocateDirect));
            } else if (data instanceof Float) {
                handleSend(() -> sendFloat((Float) data, allocateDirect));
            } else if (data instanceof Double) {
                handleSend(() -> sendDouble((Double) data, allocateDirect));
            } else if (data instanceof Character) {
                handleSend(() -> sendChar((Character) data, allocateDirect));
            } else {
                throw new IllegalArgumentException("Invalid data type: " + data.getClass().getName());
            }
            sendOutput();
        });
    }

    private void handleSend(SendTask sendTask) {
        try {
            sendTask.execute();
        } catch (IOException e) {
            System.err.println("Error while sending data: " + e.getMessage());
            e.printStackTrace();
        }
    }

}
