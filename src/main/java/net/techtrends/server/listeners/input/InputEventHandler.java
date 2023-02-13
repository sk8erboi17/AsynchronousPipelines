package net.techtrends.server.listeners.input;

import net.techtrends.server.events.ResponseCallback;
import net.techtrends.server.events.input.ReadingDataListener;

import java.nio.channels.AsynchronousSocketChannel;

/**
 * InputEventHandler is an interface that provides the handle method to be implemented by the developers.
 * <p>
 * The handle method takes in three parameters:
 * 1) asyncServerSocket: an instance of AsynchronousSocketChannel, representing the server socket for incoming connection requests.
 * 2) dataListener: an instance of DataListener, to listen for incoming data from the connected clients.
 * 3) callback: an instance of ResponseCallback, to send the response back to the connected clients.
 * <p>
 * The handle method is called whenever a new connection is made and it is responsible for processing the incoming data,
 * listening for incoming data, and sending response back to the connected clients.
 */

public interface InputEventHandler<T> {

    void handle(AsynchronousSocketChannel asyncServerSocket, ReadingDataListener<T> dataListener, ResponseCallback<T> callback);
}
