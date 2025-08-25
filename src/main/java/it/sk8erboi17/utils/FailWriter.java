package it.sk8erboi17.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public final class FailWriter {

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE_NAME = "log.txt";
    private static final int BUFFER_SIZE = 4096;
    private static final int QUEUE_CAPACITY = 1024;

    private static final FileChannel fileChannel;
    private static final ByteBuffer writeBuffer;
    private static final BlockingQueue<byte[]> messageQueue;
    private static final ExecutorService writerExecutor;
    private static final AtomicBoolean running = new AtomicBoolean(true);

    static {
        try {
            Path filePath = Paths.get(LOG_DIR, LOG_FILE_NAME);

            Files.createDirectories(filePath.getParent());

            fileChannel = FileChannel.open(filePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.APPEND);

            writeBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
            messageQueue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
            writerExecutor = Executors.newSingleThreadExecutor();

            startWriterThread();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    shutdown();
                } catch (IOException | InterruptedException e) {
                    System.err.println("Error closing log resources during shutdown: " + e.getMessage());
                }
            }));

        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize FailWriter", e);
        }
    }

    private static void startWriterThread() {
        writerExecutor.submit(() -> {
            while (running.get() || !messageQueue.isEmpty()) {
                try {
                    byte[] message = messageQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (message != null) {
                        writeToBuffer(message);
                    }
                    // Flush if buffer is not empty and no more messages for a while
                    if (message == null && writeBuffer.position() > 0) {
                        flushBuffer();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Writer thread interrupted: " + e.getMessage());
                    break;
                } catch (IOException e) {
                    System.err.println("Error writing to log file: " + e.getMessage());
                }
            }
            // Final flush before exiting
            try {
                flushBuffer();
            } catch (IOException e) {
                System.err.println("Error during final flush: " + e.getMessage());
            }
        });
    }

    public static void writeFile(String message, Throwable e) {
        message = message + " " +e.getMessage();
        Objects.requireNonNull(message, "Message cannot be null");

        // Use ByteArrayOutputStream to build the formatted message in bytes
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            String className = stackTrace.length > 0 ? stackTrace[0].getClassName() : " Unknown";
            String methodName = stackTrace.length > 0 ? stackTrace[0].getMethodName() : " Unknown";
            String now = Instant.now().toString();

            // Building the formatted message with low-level string concatenation and byte conversion
            baos.write("[".getBytes(StandardCharsets.UTF_8));
            baos.write(now.getBytes(StandardCharsets.UTF_8));
            baos.write("] Error in ".getBytes(StandardCharsets.UTF_8));
            baos.write(className.getBytes(StandardCharsets.UTF_8));
            baos.write(".".getBytes(StandardCharsets.UTF_8));
            baos.write(methodName.getBytes(StandardCharsets.UTF_8));
            baos.write("(): ".getBytes(StandardCharsets.UTF_8));
            baos.write(message.getBytes(StandardCharsets.UTF_8));
            baos.write(" - StackTrace: ".getBytes(StandardCharsets.UTF_8));

            // Iterate through the stack trace and write each element on a new line
            if (stackTrace.length > 0) {
                baos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
                for (StackTraceElement element : stackTrace) {
                    baos.write(("\t" + element.toString()).getBytes(StandardCharsets.UTF_8));
                    baos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
                }
            } else {
                baos.write("[]".getBytes(StandardCharsets.UTF_8));
                baos.write(System.lineSeparator().getBytes(StandardCharsets.UTF_8));
            }


            if (!running.get()) {
                System.err.println("FailWriter is shut down. Cannot write message: " + message);
                return;
            }
            try {
                messageQueue.put(baos.toByteArray());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                System.err.println("Failed to add message to queue: " + ex.getMessage());
            }
        } catch (IOException ex) {
            System.err.println("Error creating log message: " + ex.getMessage());
        }
    }


    private static void writeToBuffer(byte[] data) throws IOException {
        int offset = 0;
        while (offset < data.length) {
            int bytesToWrite = Math.min(data.length - offset, writeBuffer.remaining());
            writeBuffer.put(data, offset, bytesToWrite);
            offset += bytesToWrite;

            if (!writeBuffer.hasRemaining()) {
                flushBuffer();
            }
        }
    }

    private static void flushBuffer() throws IOException {
        if (writeBuffer.position() == 0) return;

        writeBuffer.flip();
        while (writeBuffer.hasRemaining()) {
            fileChannel.write(writeBuffer);
        }
        fileChannel.force(false);
        writeBuffer.clear();
    }

    public static void shutdown() throws IOException, InterruptedException {
        running.set(false);
        writerExecutor.shutdown();
        if (!writerExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
            System.err.println("Writer thread did not terminate in time. Forcing shutdown.");
            writerExecutor.shutdownNow();
        }
        // Final flush in case there are messages left in the queue or buffer
        flushBuffer();
        fileChannel.close();
    }
}
