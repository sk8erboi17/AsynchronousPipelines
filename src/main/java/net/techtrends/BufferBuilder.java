package net.techtrends;

import java.nio.ByteBuffer;

public class BufferBuilder {
    private boolean allocateDirect;

    private int initialSize;

    public BufferBuilder setInitSize(int initSize){
        this.initialSize = initSize;
        return this;
    }

    public BufferBuilder allocateDirect(boolean allocateDirect){
        this.allocateDirect = allocateDirect;
        return this;
    }

    public ByteBuffer build(){
        return allocateDirect ? ByteBuffer.allocateDirect(initialSize) : ByteBuffer.allocate(initialSize);
    }

}
