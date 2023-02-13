package net.techtrends.client.socket;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * This class is an abstract class that represents a base for reading and writing
 * data to a server using an AsynchronousSocketChannel and a ByteBuffer.
 */
public abstract class SocketThreadIO {

    protected final AsynchronousSocketChannel socketChannel;

    protected final ByteBuffer buffer;
    protected final int bytebuff;

    /**
     * Constructor for the class
     *
     * @param socketChannel The asynchronous socket channel to use for communication
     * @param bytebuff The size of the buffer to use
     */
    public SocketThreadIO(AsynchronousSocketChannel socketChannel, int bytebuff) {
        this.socketChannel = socketChannel;
        this.bytebuff = bytebuff;
        this.buffer = ByteBuffer.allocate(bytebuff);
    }

    /**
     * Abstract method that should be implemented by subclasses to start the reading or writing thread.
     *
     * @throws InterruptedException
     */
    public abstract void startThread() throws InterruptedException;
}
