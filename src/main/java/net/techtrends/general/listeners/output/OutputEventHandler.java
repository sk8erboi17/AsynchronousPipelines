package net.techtrends.general.listeners.output;

/**
 * The OutputEventHandler interface represents a contract for handling output events.
 * Implementing classes should define their behavior for processing and sending output
 * data by providing an implementation for the handle() method.
 * This interface is especially useful when working with systems that require output data
 * to be sent or processed in different ways.
 */
public interface OutputEventHandler {


    /**
     * Handles an output event with the given parameters.
     *
     * @param allocateDirect A boolean value that indicates whether the event should allocate resources directly.
     *                       If true, the implementing class should allocate resources directly for processing the event.
     *                       If false,the implementing class should use a different method for resource allocation.
     * @param value          The Object representing the data to be output by the event handler.
     *                       This can be any type of data, such as a string, number, or custom object, depending on the requirements of the implementing class.
     */
    void handle(boolean allocateDirect, Object value);

}
