package it.sk8erboi17.network.transformers.pool;

import it.sk8erboi17.exception.IllegalSizeException;
import it.sk8erboi17.exception.MaxBufferSizeExceededException;
import it.sk8erboi17.utils.FailWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

    public class ByteBuffersPool {
    private static final ByteBuffersPool instance = new ByteBuffersPool();

    private static final int POOL_SIZE = 40;
    private static final int SMALL_SIZE = 256;
    private static final int MEDIUM_SIZE = 4096;
    public static final int LARGE_SIZE = 65536;
    private static final Logger log = LoggerFactory.getLogger(ByteBuffersPool.class);

    private final BlockingQueue<ByteBuffer> smallBuffers = new ArrayBlockingQueue<>(POOL_SIZE);
    private final BlockingQueue<ByteBuffer> mediumBuffers = new ArrayBlockingQueue<>(POOL_SIZE);
    private final BlockingQueue<ByteBuffer> largeBuffers = new ArrayBlockingQueue<>(POOL_SIZE);

    private ByteBuffersPool() {
        for (int i = 0; i < POOL_SIZE; i++) {
            try {
                smallBuffers.put(ByteBuffer.allocateDirect(SMALL_SIZE));
                mediumBuffers.put(ByteBuffer.allocateDirect(MEDIUM_SIZE));
                largeBuffers.put(ByteBuffer.allocateDirect(LARGE_SIZE));
            } catch (InterruptedException e) {
                log.error("Error while creating pool {} ", e.getMessage(),e);
                FailWriter.writeFile("Error while creating pool ", e);
                Thread.currentThread().interrupt();

            }
        }
    }

    public ByteBuffer acquire(int size) throws InterruptedException, MaxBufferSizeExceededException {
        ByteBuffer byteBuffer;
        if(size <= SMALL_SIZE){
            byteBuffer = smallBuffers.take();
        }else if(size <= MEDIUM_SIZE){
            byteBuffer = mediumBuffers.take();
        }else if(size <= LARGE_SIZE){
            byteBuffer = largeBuffers.take();
        }else {
            throw new MaxBufferSizeExceededException("Dimensione richiesta (" + size + ") non valida o supera la massima consentita (" + LARGE_SIZE + ")");
        }

        byteBuffer.clear();
        return byteBuffer;
    }

    public void release(ByteBuffer byteBuffer) throws InterruptedException {
        if(byteBuffer == null) {
            throw new NullPointerException("Null buffer size");
        }
        int size = byteBuffer.capacity();

        if (size != SMALL_SIZE && size != MEDIUM_SIZE && size != LARGE_SIZE)  {
            throw new IllegalArgumentException("Attempted to release a buffer with an illegal capacity: " + size);
        }

        if (size == SMALL_SIZE) {
            smallBuffers.put(byteBuffer);
        } else if (size == MEDIUM_SIZE) {
            mediumBuffers.put(byteBuffer);
        } else {
            largeBuffers.put(byteBuffer);
        }

    }


    public static ByteBuffersPool getInstance() {
        return instance;
    }

}