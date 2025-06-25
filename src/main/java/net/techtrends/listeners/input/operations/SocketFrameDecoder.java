package net.techtrends.listeners.input.operations;

import net.techtrends.listeners.input.operations.ListenData;
import net.techtrends.listeners.response.Callback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Optimized SocketFrameDecoder for high performance.
 * Responsible for framing messages within a TCP stream.
 * Accumulates bytes from channels, identifies complete messages based on
 * start (0x01) and end (0x00) markers, and extracts payloads for subsequent decoding.
 */
public class SocketFrameDecoder {

    private static final Logger LOGGER = Logger.getLogger(SocketFrameDecoder.class.getName());
    //TODO VULN - INSERT A FIXED LENTH  FOR AVOID TRUNCATE MEX
    private static final byte START_MARKER = 0x01;
    private static final byte END_MARKER = 0x00;

    // Buffer pool to reuse ByteBuffers and reduce allocations
    private static final ThreadLocal<ByteBuffer> TEMP_BUFFER_POOL =
            ThreadLocal.withInitial(() -> ByteBuffer.allocate(8192));

    // Map to hold client buffers with additional metadata
    private final ConcurrentHashMap<AsynchronousSocketChannel, ClientBuffer> clientBuffers = new ConcurrentHashMap<>();
    private final int initialBufferSize;
    private final int maxFrameLength;
    private final ListenData listenDataProcessor;

    /**
     * Wrapper for client buffer with additional metadata for optimization
     */
    private static class ClientBuffer {
        private ByteBuffer buffer;
        int frameStartPos = -1; // Position of the last START_MARKER found

        public ClientBuffer(int initialSize) {
            this.buffer = ByteBuffer.allocate(initialSize);
        }

        void reset() {
            buffer.clear();
            frameStartPos = -1;
        }
    }

    /**
     * Constructs a new optimized SocketFrameDecoder.
     *
     * @param initialBufferSize The initial size of the accumulation buffer for each client.
     * @param maxFrameLength The maximum allowed size for a single frame (message).
     * @param listenDataProcessor The ListenData instance that will process the extracted payloads.
     */
    public SocketFrameDecoder(int initialBufferSize, int maxFrameLength, ListenData listenDataProcessor) {
        if (initialBufferSize <= 0) {
            throw new IllegalArgumentException("initialBufferSize must be positive.");
        }
        if (maxFrameLength <= 0 || maxFrameLength < initialBufferSize) {
            throw new IllegalArgumentException("maxFrameLength must be positive and greater than or equal to initialBufferSize.");
        }
        if (listenDataProcessor == null) {
            throw new IllegalArgumentException("listenDataProcessor cannot be null.");
        }

        this.initialBufferSize = initialBufferSize;
        this.maxFrameLength = maxFrameLength;
        this.listenDataProcessor = listenDataProcessor;
    }

    /**
     * Optimized decode method with reduced memory allocations and improved parsing performance.
     * This method is called whenever data is read from a channel.
     * It appends new bytes to the client's accumulation buffer,
     * searches for complete frames, and passes them to the ListenData processor.
     *
     * @param clientChannel The channel from which the data was read.
     * @param newlyReadBuffer The ByteBuffer containing the bytes just read from the socket.
     * @param callback The original callback for the complete message, passed to ListenData.
     */
    public void decode(AsynchronousSocketChannel clientChannel, ByteBuffer newlyReadBuffer, Callback callback) {
        // Get or create the client buffer wrapper
        ClientBuffer clientBuffer = clientBuffers.computeIfAbsent(clientChannel,
                k -> new ClientBuffer(initialBufferSize));

        newlyReadBuffer.flip();
        int bytesToAppend = newlyReadBuffer.remaining();

        // Optimized capacity check and buffer resize
        if (!ensureCapacity(clientBuffer, bytesToAppend, clientChannel)) {
            newlyReadBuffer.clear();
            return;
        }

        // Append new data to client buffer
        clientBuffer.buffer.put(newlyReadBuffer);
        newlyReadBuffer.clear();

        // Optimized frame processing with reduced buffer operations
        processFrames(clientBuffer, clientChannel, callback);
    }

    /**
     * Ensures buffer has sufficient capacity with optimized resize strategy.
     * Uses exponential growth to minimize resize operations.
     *
     * @param clientBuffer The client buffer wrapper
     * @param additionalBytes Number of additional bytes needed
     * @param channel The client channel for error logging
     * @return true if capacity is ensured, false if max frame length exceeded
     */
    private boolean ensureCapacity(ClientBuffer clientBuffer, int additionalBytes, AsynchronousSocketChannel channel) {
        ByteBuffer buffer = clientBuffer.buffer;

        if (buffer.remaining() >= additionalBytes) {
            return true; // Sufficient space available
        }

        int currentPosition = buffer.position();
        int dataSize = currentPosition;
        // Exponential growth strategy to minimize resize operations
        int newCapacity = Math.max(buffer.capacity() * 2, dataSize + additionalBytes);

        if (newCapacity > maxFrameLength) {
            logError("Message exceeds maxFrameLength (" + maxFrameLength + " bytes). Discarding data.", channel);
            clientBuffer.reset();
            return false;
        }

        // Optimized buffer resize with minimal data copying
        ByteBuffer newBuffer = ByteBuffer.allocate(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        clientBuffer.buffer = newBuffer;

        return true;
    }

    /**
     * Optimized frame processing with single-pass parsing and reduced memory allocations.
     * Processes all complete frames in the buffer in one iteration.
     *
     * @param clientBuffer The client buffer wrapper
     * @param channel The client channel
     * @param callback The callback for processed frames
     */
    private void processFrames(ClientBuffer clientBuffer, AsynchronousSocketChannel channel, Callback callback) {
        ByteBuffer buffer = clientBuffer.buffer;
        buffer.flip();

        int position = 0;
        int limit = buffer.limit();
        // Optimize array access when possible for better performance
        byte[] arrayBuffer = buffer.hasArray() ? buffer.array() : null;
        int arrayOffset = buffer.hasArray() ? buffer.arrayOffset() : 0;

        while (position < limit) {
            // Optimized search for START_MARKER
            int startPos = findNextMarker(buffer, arrayBuffer, arrayOffset, position, limit, START_MARKER);
            if (startPos == -1) {
                // No START_MARKER found, preserve remaining data
                break;
            }

            if (startPos > position) {
                // Skip noise bytes and log if necessary
                logNoise(channel, position, startPos);
                position = startPos;
            }

            // Search for END_MARKER after START_MARKER
            int contentStart = startPos + 1;
            if (contentStart >= limit) {
                // START_MARKER is the last byte, wait for more data
                break;
            }

            int endPos = findNextMarker(buffer, arrayBuffer, arrayOffset, contentStart, limit, END_MARKER);
            if (endPos == -1) {
                // Incomplete frame, preserve from START_MARKER
                position = startPos;
                break;
            }

            // Complete frame found: [START_MARKER][Content][END_MARKER]
            int payloadLength = endPos - contentStart;

            try {
                // Create optimized payload buffer with minimal copying
                ByteBuffer payloadBuffer = createPayloadBuffer(buffer, arrayBuffer, arrayOffset, contentStart, payloadLength);
                listenDataProcessor.listen(START_MARKER, payloadBuffer, callback);
            } catch (Exception e) {
                logError("Error processing decoded frame: " + e.getMessage(), channel);
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Frame processing error", e);
                }
            }

            position = endPos + 1; // Continue after END_MARKER
        }

        // Compact buffer preserving unprocessed data
        compactBuffer(clientBuffer, position);
    }

    /**
     * Optimized marker search in buffer using direct array access when possible.
     * Falls back to buffer access for direct buffers.
     *
     * @param buffer The source buffer
     * @param arrayBuffer Direct array access (null for direct buffers)
     * @param arrayOffset Array offset for heap buffers
     * @param start Start search position
     * @param limit End search position
     * @param marker The marker byte to find
     * @return Position of marker or -1 if not found
     */
    private int findNextMarker(ByteBuffer buffer, byte[] arrayBuffer, int arrayOffset,
                               int start, int limit, byte marker) {
        if (arrayBuffer != null) {
            // Optimized search on direct array access
            for (int i = start; i < limit; i++) {
                if (arrayBuffer[arrayOffset + i] == marker) {
                    return i;
                }
            }
        } else {
            // Fallback for direct buffers
            for (int i = start; i < limit; i++) {
                if (buffer.get(i) == marker) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Creates payload buffer in an optimized way by reusing buffers when possible.
     * Uses thread-local buffer pool to minimize allocations.
     *
     * @param sourceBuffer The source buffer containing the payload
     * @param arrayBuffer Direct array access (null for direct buffers)
     * @param arrayOffset Array offset for heap buffers
     * @param start Start position of payload
     * @param length Length of payload
     * @return ByteBuffer containing the payload data
     */
    private ByteBuffer createPayloadBuffer(ByteBuffer sourceBuffer, byte[] arrayBuffer,
                                           int arrayOffset, int start, int length) {
        if (length == 0) {
            return ByteBuffer.allocate(0);
        }

        ByteBuffer payloadBuffer;

        // Reuse buffer from pool if possible to reduce allocations
        ByteBuffer tempBuffer = TEMP_BUFFER_POOL.get();
        if (tempBuffer.capacity() >= length) {
            tempBuffer.clear();
            payloadBuffer = tempBuffer.slice();
            payloadBuffer.limit(length);
        } else {
            payloadBuffer = ByteBuffer.allocate(length);
        }

        if (arrayBuffer != null) {
            // Optimized copy from array
            payloadBuffer.put(arrayBuffer, arrayOffset + start, length);
        } else {
            // Copy from direct buffer
            ByteBuffer slice = sourceBuffer.duplicate();
            slice.position(start);
            slice.limit(start + length);
            payloadBuffer.put(slice);
        }

        payloadBuffer.flip();
        return payloadBuffer;
    }

    /**
     * Optimized buffer compaction with efficient data movement.
     * Uses System.arraycopy for heap buffers for better performance.
     *
     * @param clientBuffer The client buffer wrapper
     * @param processedPosition Position up to which data has been processed
     */
    private void compactBuffer(ClientBuffer clientBuffer, int processedPosition) {
        ByteBuffer buffer = clientBuffer.buffer;
        int remaining = buffer.limit() - processedPosition;

        if (remaining > 0) {
            // Move unprocessed data to the beginning
            if (buffer.hasArray()) {
                // Optimized array copy for heap buffers
                byte[] array = buffer.array();
                int offset = buffer.arrayOffset();
                System.arraycopy(array, offset + processedPosition, array, offset, remaining);
            } else {
                // Fallback for direct buffers
                for (int i = 0; i < remaining; i++) {
                    buffer.put(i, buffer.get(processedPosition + i));
                }
            }
        }

        buffer.position(remaining);
        buffer.limit(buffer.capacity());
    }

    /**
     * Optimized error logging to avoid expensive string operations when not needed.
     *
     * @param message The error message
     * @param channel The client channel
     */
    private void logError(String message, AsynchronousSocketChannel channel) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            try {
                LOGGER.warning("ERROR: " + message + " Channel: " + channel.getRemoteAddress());
            } catch (IOException e) {
                LOGGER.warning("ERROR: " + message + " (failed to get remote address)");
            }
        }
    }

    /**
     * Optimized noise logging for debugging protocol issues.
     *
     * @param channel The client channel
     * @param start Start position of noise
     * @param end End position of noise
     */
    private void logNoise(AsynchronousSocketChannel channel, int start, int end) {
        if (LOGGER.isLoggable(Level.FINE)) {
            try {
                LOGGER.info("Protocol noise: skipped " + (end - start) + " bytes from " + channel.getRemoteAddress());
            } catch (IOException e) {
                LOGGER.info("Protocol noise: skipped " + (end - start) + " bytes (failed to get remote address)");
                e.printStackTrace();
            }
        }
    }

    /**
     * Optimized cleanup for disconnected clients with proper resource management.
     * Important to call this when a client disconnects to release memory.
     *
     * @param clientChannel The channel of the disconnected client.
     */
    public void onClientDisconnected(AsynchronousSocketChannel clientChannel) {
        ClientBuffer removed = clientBuffers.remove(clientChannel);
        if (removed != null && LOGGER.isLoggable(Level.INFO)) {
            try {
                LOGGER.info("Removed buffer for disconnected client: " + clientChannel.getRemoteAddress());
            } catch (IOException e) {
                LOGGER.info("Removed buffer for disconnected client (address unavailable)");
            }
        }
    }

}