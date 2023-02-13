package net.techtrends.server.events.output;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class WriteCharOutputEvent extends WritingDataListener<Character> {
    @Override
    public void writeDataToServer(AsynchronousSocketChannel asyncServerSocket, Character value) {
        byte[] bytes = ByteBuffer.allocate(Character.BYTES).putChar(value).array();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        asyncServerSocket.write(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                buffer.clear();
                setWriting(false);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
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
