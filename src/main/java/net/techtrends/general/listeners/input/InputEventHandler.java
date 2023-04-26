package net.techtrends.general.listeners.input;

import net.techtrends.general.listeners.ResponseCallback;

import java.nio.channels.AsynchronousSocketChannel;
/**
 *  The InputEventHandler interface represents a contract for handling input events.
 *  Implementing classes should define their behavior for handling events by
 *  providing an implementation for the handle() method.
 *  This interface is especially useful when working with event-driven systems, such as
 *  user interfaces or network communication, where events need to be processed and responded to.
 */
public interface InputEventHandler {

    /**
     * Handles an input event with the given parameters.
     * @param allocateDirect A boolean value that indicates whether the event should allocate resources directly.
     * If true, the implementing class should allocate resources directly for processing the event.
     * If false, the implementing class should use a different method for resource allocation.
     * @param callback The ResponseCallback instance that should be invoked once the event has been processed. This allows the
     * calling code to receive a notification when the event handling is completed, and take appropriate action if necessary.
     */
    void handle(boolean allocateDirect, ResponseCallback callback);
}
