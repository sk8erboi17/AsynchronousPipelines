package net.techtrends.server.events.input;

import net.techtrends.server.events.ResponseCallback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ReadFloatInputEvent extends ReadingDataListener<Float> {
    @Override
    public void readNextData(AsynchronousSocketChannel asyncServerSocket, ResponseCallback<Float> callback) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        asyncServerSocket.read(buffer, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                float floatData = buffer.getFloat();
                if (callback != null) {
                    callback.complete(floatData);
                }

                buffer.clear();
                asyncServerSocket.read(buffer, null, this);
                setReading(false);

            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                System.err.println("An error occurred while reading data: " + exc.getMessage());
                System.err.println("Closing connection...");
                try {
                    asyncServerSocket.close();
                } catch (IOException e) {
                    System.err.println("An error occurred while closing the connection: " + e.getMessage());
                }
            }
        });
    }
}
