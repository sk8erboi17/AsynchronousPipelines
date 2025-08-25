package it.sk8erboi17;

import java.nio.channels.AsynchronousSocketChannel;


public interface ConnectionRequest {
    /**
     * Handles a newly accepted client connection.
     *
     * @param socketChannel The accepted AsynchronousSocketChannel.
     * @param attachment A context object passed from the accept operation.
     */
    void acceptConnection(AsynchronousSocketChannel socketChannel, Object attachment);

    /**
     * Handles a failure during the connection acceptance process.
     *
     * @param exc The exception that occurred.
     */
    void onConnectionFailed(Throwable exc);

}