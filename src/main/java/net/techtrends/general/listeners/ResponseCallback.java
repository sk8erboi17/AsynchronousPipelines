package net.techtrends.general.listeners;

/**
 * Interface ResponseCallback
 * The ResponseCallback interface is used to complete a task and receive a response.
 * The interface provides a single method "complete" that takes a generic parameter T.
 * The purpose of the interface is to provide a callback mechanism to receive a response
 * after a task is completed. This is useful for asynchronous operations where the result
 * may not be immediately available.
 * To use the interface, create a class that implements the interface and overrides the
 * complete method. Then pass an instance of the implementation class to the task that
 * requires the response. When the task is completed, it will call the complete method
 * and pass the response as the argument.
 *
 */

public interface ResponseCallback {

    void complete(Object o);
}
