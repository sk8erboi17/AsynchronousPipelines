package net.techtrends.server.listeners.output;

import net.techtrends.server.events.output.WritingDataListener;

import java.nio.channels.AsynchronousSocketChannel;

/*
 * OutputEventHandler is an interface that specifies a contract for handling output events in a server.
 * The handle method takes three parameters: an AsynchronousSocketChannel, a data type, and a WritingDataListener.
 * This interface provides a way to write the data to the specified asynchronous socket channel and handle any errors
 * that might occur during the writing process.
*/
public interface OutputEventHandler<T> {

    /**
     * This method handles the output event that is going to be written
     * on the specified asynchronous socket channel.
     *
     * @param asyncServerSocket The asynchronous socket channel on which the data is going to be written.
     * @param type The type of data that is going to be written.
     * @param dataListener The writing data listener that is used to manage the writing process.
     */
    void handle(AsynchronousSocketChannel asyncServerSocket, T type, WritingDataListener<T> dataListener);

}
