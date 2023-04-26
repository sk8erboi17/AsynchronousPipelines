package net.techtrends.general.listeners.interfaces;

import java.io.IOException;


/**
 * The PrimitiveWriter functional interface represents a contract for writing
 * primitive data types to a DataOutputStream. Implementing classes or lambda
 * expressions should define their behavior for writing data to the given stream
 * by providing an implementation for the write() method.
 * This interface is particularly useful when working with serialization, file
 * I/O, or network communication, where primitive data types need to be written to a stream.
 */

@FunctionalInterface
public interface SendTask {

    /**
     * Executes the task, which may involve sending data or performing other I/O operations.
     *
     * @throws IOException If an I/O error occurs during the execution of the task.
     */

    void execute() throws IOException;
}