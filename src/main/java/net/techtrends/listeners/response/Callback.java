package net.techtrends.listeners.response;

/**
 * This interface defines methods for handling the completion of asynchronous operations.
 *  complete is called when an operation finishes successfully, while completeExceptionally is invoked if an error occurs.
 * Implementations of Callback handle the logic for processing results or exceptions
 */
public interface Callback {

    // Method to handle successful completion of an operation.
    void complete(Object o);

    // Method to handle an exception that occurs during the operation.
    void completeExceptionally(Throwable throwable);

}
