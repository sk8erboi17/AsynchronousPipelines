package net.techtrends.general.listeners;

/**
 * This is an interface named "ResponseCallback". It contains two methods:
 * <p>
 * 1. complete(Object o) - This method is called when the task is completed successfully.
 * <p>
 * 2. completeExceptionally(Throwable throwable) - This method is called when an exception is encountered during the execution of the task.
 * <p>
 * The purpose of this interface is to provide a way to handle the result of an asynchronous task and any errors that may occur during its execution.
 * It can be used in various contexts such as network communication, database operations, or any other long-running tasks.
 */

public interface ResponseCallback {

    void complete(Object o);

    void completeExceptionally(Throwable throwable);
}
