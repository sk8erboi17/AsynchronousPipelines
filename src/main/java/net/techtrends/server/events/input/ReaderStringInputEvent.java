package net.techtrends.server.events.input;

import net.techtrends.server.events.ResponseCallback;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.StandardCharsets;

public class ReaderStringInputEvent extends ReadingDataListener<String> {


    @Override
    protected void readNextData(AsynchronousSocketChannel asyncServerSocket, ResponseCallback<String> callback) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        asyncServerSocket.read(buffer, null, new CompletionHandler<>() {
            @Override
            public void completed(Integer result, Object attachment) {
                String data = new String(buffer.array(), StandardCharsets.UTF_8).trim();
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
