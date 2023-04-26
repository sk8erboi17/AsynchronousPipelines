package net.techtrends.general.listeners.interfaces;

import java.io.DataOutputStream;
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
public interface PrimitiveWriter {

    /**
     * Writes primitive data to the provided DataOutputStream.
     *
     * @param dataOutputStream The DataOutputStream to which the primitive data
     *                         should be written. This stream is typically connected to a file, socket, or other data destination.
     * @throws IOException If an I/O error occurs while writing to the stream.
     */
    void write(DataOutputStream dataOutputStream) throws IOException;
}
