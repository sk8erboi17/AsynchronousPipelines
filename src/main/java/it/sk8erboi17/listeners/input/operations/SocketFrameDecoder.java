package it.sk8erboi17.listeners.input.operations;

import it.sk8erboi17.listeners.response.Callback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized and Secure SocketFrameDecoder for high performance.
 * Responsible for framing messages based on a length-prefixed protocol.
 * The protocol is [START_MARKER (1 byte)][LENGTH (4 bytes)][DATA_TYPE (1 byte)][PAYLOAD (N bytes)]
 */
public class SocketFrameDecoder {

    private static final Logger LOGGER = Logger.getLogger(SocketFrameDecoder.class.getName());

    private static final byte START_MARKER = 0x01;

    private static final int HEADER_LENGTH = 5;


    private final ConcurrentHashMap<AsynchronousSocketChannel, ByteBuffer> clientBuffers = new ConcurrentHashMap<>();
    private final int initialBufferSize;
    private final int maxFrameLength;
    private final ListenData listenDataProcessor;

    public SocketFrameDecoder(int initialBufferSize, int maxFrameLength, ListenData listenDataProcessor) {
        if (initialBufferSize <= 0 || maxFrameLength <= 0) {
            throw new IllegalArgumentException("Buffer sizes must be positive.");
        }
        if (listenDataProcessor == null) {
            throw new IllegalArgumentException("listenDataProcessor cannot be null.");
        }
        this.initialBufferSize = initialBufferSize;
        this.maxFrameLength = maxFrameLength;
        this.listenDataProcessor = listenDataProcessor;
    }

    public void decode(AsynchronousSocketChannel clientChannel, ByteBuffer newlyReadBuffer, Callback callback) {
        ByteBuffer clientBuffer = clientBuffers.computeIfAbsent(clientChannel, k -> ByteBuffer.allocate(initialBufferSize));

        newlyReadBuffer.flip();
        ByteBuffer combinedBuffer = appendToClientBuffer(clientBuffer, newlyReadBuffer);
        clientBuffers.put(clientChannel, combinedBuffer);

        combinedBuffer.flip();
        processFrames(clientChannel, combinedBuffer, callback);
        combinedBuffer.compact();
    }

    private void processFrames(AsynchronousSocketChannel channel, ByteBuffer buffer, Callback callback) {
        while (true) {
            if (!findAndSkipToStartMarker(buffer)) {
                //if there is no marker, stop checking
                return;
            }

            buffer.mark(); //set position after marker

            if (buffer.remaining() < Integer.BYTES) {
                // there is no info about the data
                buffer.reset(); // reset buffer
                return;
            }

            int frameLength = buffer.getInt();

            // check the frameLength
            if (frameLength <= 0 || frameLength > maxFrameLength) {
                logError("Invalid frame length received: " + frameLength + ". Max allowed: " + maxFrameLength + ". Closing connection.", channel);
                try {
                    channel.close();
                } catch (IOException e) {
                    logError("Error closing channel after invalid frame length: " + e.getMessage(), channel);
                }
                buffer.clear();
                return;
            }

            if (buffer.remaining() < frameLength) {
                // the frame is incomplete
                buffer.reset();
                return;
            }

            // read the payload
            try {
                byte dataTypeMarker = buffer.get();
                int payloadLength = frameLength - 1;

                ByteBuffer payloadBuffer = ByteBuffer.allocate(payloadLength);
                buffer.get(payloadBuffer.array());
                listenDataProcessor.listen(dataTypeMarker, payloadBuffer, callback);

            } catch (Exception e) {
                logError("Error processing decoded frame: " + e.getMessage(), channel);
            }
        }
    }

    private boolean findAndSkipToStartMarker(ByteBuffer buffer) {
        while (buffer.hasRemaining()) {
            if (buffer.get() == START_MARKER) {
                return true;
            }
        }
        return false;
    }

    private ByteBuffer appendToClientBuffer(ByteBuffer clientBuffer, ByteBuffer newBytes) {
        if (clientBuffer.remaining() < newBytes.remaining()) {
            int newSize = Math.max(clientBuffer.capacity() * 2, clientBuffer.position() + newBytes.remaining());
            if (newSize > maxFrameLength) {
                throw new RuntimeException("Buffer cannot grow beyond maxFrameLength");
            }
            ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
            clientBuffer.flip();
            newBuffer.put(clientBuffer);
            clientBuffer = newBuffer;
        }
        clientBuffer.put(newBytes);
        return clientBuffer;
    }

    private void logError(String message, AsynchronousSocketChannel channel) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            try {
                LOGGER.warning("ERROR: " + message + " Channel: " + channel.getRemoteAddress());
            } catch (IOException e) {
                LOGGER.warning("ERROR: " + message + " (failed to get remote address)");
            }
        }
    }

    public void onClientDisconnected(AsynchronousSocketChannel clientChannel) {
        clientBuffers.remove(clientChannel);
        LOGGER.info("Removed buffer for disconnected client.");
    }
}