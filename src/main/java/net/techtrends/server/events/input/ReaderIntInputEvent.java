package net.techtrends.server.events.input;

import net.techtrends.server.events.ResponseCallback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class ReaderIntInputEvent extends ReadingDataListener<Integer> {
    protected void readNextData(AsynchronousSocketChannel asyncServerSocket, ResponseCallback<Integer> callback) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        asyncServerSocket.read(buffer, null, new CompletionHandler<Integer, Object>() {
            @Override
            public void completed(Integer result, Object attachment) {
                asyncServerSocket.read(buffer,null,this);
                int data = buffer.getInt();

                if (callback != null) {
                    callback.complete(data);
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
