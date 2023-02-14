package net.techtrends.server.events.output;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * WritingDataListener is a class that allows you to write data to a client.
 * <p>
 * The class handles writing data to asynchronous socket channel.
 * <p>
 * This class contains methods to handle writing of data and error handling.
 *
 * @param <T> The type of data that is being written.
 */
public abstract class WritingDataListener<T> {
    private volatile boolean isWriting;


    /**
     * Checks if the server socket channel is open.
     *
     * @param socketChannel The server socket channel to check if it is open.
     * @return True if the server socket channel is open, false otherwise.
     */
    private boolean isServerOpen(AsynchronousSocketChannel socketChannel) {
        return socketChannel.isOpen();
    }

    /**
     * Writes data to the socket.
     *
     * @param type                The data to be written to the server.
     * @param serverSocketChannel The server socket channel to write data to.
     */
    public void handleWrite(T type, AsynchronousSocketChannel serverSocketChannel) {
        if (!isWriting) {
            setWriting(true);
            if (isServerOpen(serverSocketChannel)) {
                writeDataToServer(serverSocketChannel, type);
            }
        }
    }

    /**
     * An abstract method that should be implemented by concrete implementations of this class to initiate a write operation
     * on the specified asynchronous socket channel. This method is used to write data to the server using the asynchronous socket channel.
     *
     * @param socketChannel The asynchronous socket channel to write data to.
     * @param type              The data to be written to the server. The type of data can be specified by the concrete implementation of this class.
     */
    protected abstract void writeDataToServer(AsynchronousSocketChannel socketChannel, T type);

    protected void setWriting(boolean isWriting) {
        this.isWriting = isWriting;
    }
}
