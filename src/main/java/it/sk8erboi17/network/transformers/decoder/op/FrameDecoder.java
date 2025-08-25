package it.sk8erboi17.network.transformers.decoder.op;

import it.sk8erboi17.exception.MaxFrameLengthExceededException;
import it.sk8erboi17.listeners.input.operations.ListenData;
import it.sk8erboi17.listeners.response.Callback;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Optimized and Secure SocketFrameDecoder for high performance.
 * Responsible for framing messages based on a length-prefixed protocol.
 * The protocol is [START_MARKER (1 byte)][LENGTH (4 bytes)][DATA_TYPE (1 byte)][PAYLOAD (N bytes)]
 */
public class FrameDecoder {

    private static final Logger LOGGER = Logger.getLogger(FrameDecoder.class.getName());

    private static final byte START_MARKER = 0x01;

    final int MAX_GARBAGE_TOLERANCE = 8192;

    private final ConcurrentHashMap<Channel, ByteBuffer> clientBuffers = new ConcurrentHashMap<>();
    private final int initialBufferSize;
    private final int maxFrameLength;
    private final ListenData listenDataProcessor;

    public FrameDecoder(int initialBufferSize, int maxFrameLength, ListenData listenDataProcessor) {
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

    public void decode(Channel clientChannel, ByteBuffer newlyReadBuffer, Callback callback) {
        ByteBuffer clientBuffer = clientBuffers.computeIfAbsent(clientChannel, k -> ByteBuffer.allocate(initialBufferSize));

        newlyReadBuffer.flip();
        ByteBuffer combinedBuffer = appendToClientBuffer(clientBuffer, newlyReadBuffer);
        clientBuffers.put(clientChannel, combinedBuffer);

        combinedBuffer.flip();
        processFrames(clientChannel, combinedBuffer, callback);
        combinedBuffer.compact();
    }

    private void processFrames(Channel channel, ByteBuffer buffer, Callback callback) {
        while (true) {
            if (!findAndSkipToStartMarker(buffer, MAX_GARBAGE_TOLERANCE)) {
                return;
            }

            buffer.mark();

            if (buffer.remaining() < Integer.BYTES) {
                buffer.reset();
                return;
            }

            int frameLength = buffer.getInt();

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
                buffer.reset();
                return;
            }

            try {
                byte dataTypeMarker = buffer.get();
                int actualPayloadSize = frameLength - 1;
                ByteBuffer payloadBuffer = buffer.slice();
                payloadBuffer.limit(actualPayloadSize);
                listenDataProcessor.listen(dataTypeMarker, payloadBuffer, callback);
                buffer.position(buffer.position() + actualPayloadSize);
            } catch (Exception e) {
                logError("Error processing decoded frame", e, channel);
            }
        }
    }

    private boolean findAndSkipToStartMarker(ByteBuffer buffer, int scanLimit) {
        int bytesScanned = 0;
        while (buffer.hasRemaining() && bytesScanned < scanLimit) {
            if (buffer.get() == START_MARKER) {
                return true;
            }
            bytesScanned++;
        }
        return false;
    }

    private ByteBuffer appendToClientBuffer(ByteBuffer clientBuffer, ByteBuffer newBytes) {
        if (clientBuffer.remaining() < newBytes.remaining()) {
            int newSize = Math.max(clientBuffer.capacity() * 2, clientBuffer.position() + newBytes.remaining());
            if (newSize > maxFrameLength) {
                throw new MaxFrameLengthExceededException("Buffer cannot grow beyond maxFrameLength");
            }
            ByteBuffer newBuffer = ByteBuffer.allocate(newSize);
            clientBuffer.flip();
            newBuffer.put(clientBuffer);
            clientBuffer = newBuffer;
        }
        clientBuffer.put(newBytes);
        return clientBuffer;
    }

    private void logError(String message, Channel channel) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            String channelInfo = "Channel: " + channel.toString();
            try {
                if (channel instanceof AsynchronousSocketChannel) {
                    channelInfo = "Client: " + ((AsynchronousSocketChannel) channel).getRemoteAddress().toString();
                } else if (channel instanceof SocketChannel) {
                    channelInfo = "Client: " + ((SocketChannel) channel).getRemoteAddress().toString();
                }
            } catch (IOException e) {
                channelInfo += " (failed to get remote address)";
            }
            LOGGER.warning("WARNING: " + message + " " + channelInfo);
        }
    }

    private void logError(String message, Exception e, Channel channel) {
        if (LOGGER.isLoggable(Level.WARNING)) {
            String channelInfo = "Channel: " + channel.toString();
            try {
                if (channel instanceof AsynchronousSocketChannel) {
                    channelInfo = "Client: " + ((AsynchronousSocketChannel) channel).getRemoteAddress().toString();
                } else if (channel instanceof SocketChannel) {
                    channelInfo = "Client: " + ((SocketChannel) channel).getRemoteAddress().toString();
                }
            } catch (IOException ioException) {
                channelInfo += " (failed to get remote address)";
            }
            String fullMessage = String.format("ERROR: %s %s", message, channelInfo);
            LOGGER.log(Level.WARNING, fullMessage, e);
        }
    }

    public void onClientDisconnected(Channel clientChannel) {
        clientBuffers.remove(clientChannel);
        LOGGER.info("Removed buffer for disconnected client.");
    }
}
