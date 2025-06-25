package net.techtrends.listeners.input;

import net.techtrends.exception.MaxBufferSizeExceededException;
import net.techtrends.listeners.input.operations.ListenData;
import net.techtrends.listeners.input.operations.SocketFrameDecoder;
import net.techtrends.listeners.output.AsyncChannelSocket;
import net.techtrends.listeners.response.Callback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * On the server side, InputListener handles the reception of data from the client.
 * It reads data from the AsynchronousSocketChannel into a ByteBuffer
 */

public class DataDecoder implements CompletionHandler<Integer, ByteBuffer> {

    private final ExecutorService readThread;
    private final ListenData processData;
    private final AsynchronousSocketChannel socketChannel;
    private final int bufferSize;
    private final Callback callback;
    private final SocketFrameDecoder frameDecoder; // <-- 2. Nuova istanza del frame decoder

    public DataDecoder(AsynchronousSocketChannel socketChannel, int bufferSize, Callback callback) {
        this.socketChannel = socketChannel;
        this.callback = callback;
        this.bufferSize = bufferSize;
        this.readThread = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() / 2);
        this.processData = new ListenData();
        this.frameDecoder = new SocketFrameDecoder(
                bufferSize / 2,
                bufferSize,
                this.processData
        );
    }

    @Override
    public void completed(Integer bytesRead, ByteBuffer buffer) {

        if (bytesRead == -1) { // -1 client is closed
            try {
                System.out.println("Client disconnected gracefully: " + socketChannel.getRemoteAddress());
            } catch (IOException e) {
                throw new RuntimeException(e); // TODO HANDLE
            }
            frameDecoder.onClientDisconnected(socketChannel);
            AsyncChannelSocket.closeChannelSocketChannel(socketChannel);
            return;
        }

        if (bytesRead < 0) {
            AsyncChannelSocket.closeChannelSocketChannel(socketChannel);
            throw new RuntimeException("Not enough byte to read");
        }else{
            frameDecoder.decode(socketChannel, buffer, callback);
        }

        buffer.clear();

        // Execute the next read operation in a separate thread
        readThread.execute(() -> startRead(buffer));
    }

    // Method to start reading data into the buffer
    public void startRead(ByteBuffer buffer) {
        socketChannel.read(buffer, buffer, this);
    }

    @Override
    public void failed(Throwable exc, ByteBuffer buffer) {
        String clientAddress = "Unknown Client";
        try {
            if (socketChannel.getRemoteAddress() != null) {
                clientAddress = socketChannel.getRemoteAddress().toString();
            }
        } catch (IOException e) {
            // Non riusciamo nemmeno a ottenere l'indirizzo remoto, probabile disconnessione grave
        }

        // `exc` ti dirà il motivo del fallimento.
        // Alcuni tipi comuni di eccezioni per disconnessioni/errori:
        if (exc instanceof java.nio.channels.AsynchronousCloseException) {
            System.out.println("Client " + clientAddress + " channel closed asynchronously during operation: " + exc.getMessage());
        } else if (exc instanceof java.nio.channels.ClosedChannelException) {
            System.out.println("Client " + clientAddress + " channel already closed: " + exc.getMessage());
        } else {
            System.err.println("Unexpected error for client " + clientAddress + ": " + exc.getClass().getName() + " - " + exc.getMessage());
        }

        // channel is broken
        if (socketChannel.isOpen()) {
            AsyncChannelSocket.closeChannelSocketChannel(socketChannel);
        }

    }
}
