package net.techtrends.server.events.input;

import net.techtrends.server.events.ResponseCallback;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * This abstract class provides a framework for data reading from an asynchronous server socket channel.
 *
 * @param <T> The type of data that the user wants to read from the server socket channel.
 */
public abstract class ReadingDataListener<T> {

    private volatile boolean isReading;


    /**
     * Checks if the server socket channel is open.
     *
     * @param serverSocketChannel The server socket channel to check if it is open.
     * @return True if the server socket channel is open, false otherwise.
     */
    private boolean isServerOpen(AsynchronousSocketChannel serverSocketChannel) {
        return serverSocketChannel != null ;
    }


    /**
     * Initiates a read operation on the specified asynchronous server socket channel.
     *
     * @param asyncServerSocket The asynchronous server socket channel to read data from.
     * @param callback          The response callback to be executed once data is read from the server socket channel.
     */
    public void readData(AsynchronousSocketChannel asyncServerSocket, ResponseCallback<T> callback) {
        if (isServerOpen(asyncServerSocket)) {
            if (!isReading) {
                setReading(true);
                readNextData(asyncServerSocket,callback);
            }
        }
    }

    protected void setReading(boolean reading) {
        isReading = reading;
    }

    /**
     * An abstract method that should be implemented by concrete implementations of this class to initiate a read operation
     * on the specified asynchronous server socket channel.
     *
     * @param asyncServerSocket The asynchronous server socket channel to read data from.
     */
    protected abstract void readNextData(AsynchronousSocketChannel asyncServerSocket, ResponseCallback<T> callback);
}
