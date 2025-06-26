package it.sk8erboi17.listeners.input;

import it.sk8erboi17.listeners.input.operations.ListenData;
import it.sk8erboi17.listeners.input.operations.SocketFrameDecoder;
import it.sk8erboi17.listeners.output.AsyncChannelSocket;
import it.sk8erboi17.listeners.response.Callback;

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
    private final Callback callback;
    private final SocketFrameDecoder frameDecoder;

    public DataDecoder(AsynchronousSocketChannel socketChannel, int frameLength, Callback callback, ExecutorService sharedThreadPool) {
        this.socketChannel = socketChannel;
        this.callback = callback;
        this.readThread = sharedThreadPool;
        this.processData = new ListenData();
        this.frameDecoder = new SocketFrameDecoder(
                frameLength / 2,
                frameLength,
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
