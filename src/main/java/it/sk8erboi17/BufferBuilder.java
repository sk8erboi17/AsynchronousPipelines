package it.sk8erboi17;

import java.nio.ByteBuffer;

//This utility class simplifies the creation of ByteBuffer instances.
public class BufferBuilder {
    // Flag to indicate whether to allocate a direct ByteBuffer.
    private boolean allocateDirect;

    // Initial size of the ByteBuffer to be allocated.
    private int initialSize;

    // Sets the initial size for the ByteBuffer and returns the builder instance.
    public BufferBuilder setInitSize(int initSize) {
        this.initialSize = initSize;
        return this;
    }

    // Sets the allocateDirect flag and returns the builder instance.
    public BufferBuilder allocateDirect(boolean allocateDirect) {
        this.allocateDirect = allocateDirect;
        return this;
    }

    // Builds and returns a ByteBuffer instance based on the set properties.
    public ByteBuffer build() {
        // Allocate a direct ByteBuffer if the flag is set, otherwise allocate a regular ByteBuffer.
        return allocateDirect ? ByteBuffer.allocateDirect(initialSize) : ByteBuffer.allocate(initialSize);
    }
}
