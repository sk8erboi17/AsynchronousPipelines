package net.techtrends.general.listeners.input;

import net.techtrends.general.listeners.ResponseCallback;

import java.nio.channels.AsynchronousSocketChannel;


/*
*    This is an interface representing an event handler for input events from an AsynchronousSocketChannel.
*    It provides a single method handle() that takes an AsynchronousSocketChannel and a ResponseCallback as parameters.
*    The method is responsible for reading data from the socket channel and invoking the appropriate ResponseCallback
*   method to pass the data back to the calling code.
*    @param <T> the type of data to be read from the socket channel and passed back to the calling code
*/

public interface InputEventHandler {
    /**
     * Handles input operations on the socket channel, sending the given value to the
     * client and invoking the provided callback when the operation is complete.
     *
     * @param socketChannel the socket channel to read data to
     * @param callback the callback to invoke when the read operation is complete
     */
    void handle(AsynchronousSocketChannel socketChannel, ResponseCallback callback);
}
