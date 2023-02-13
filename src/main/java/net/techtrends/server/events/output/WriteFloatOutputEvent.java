package net.techtrends.server.events.output;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

public class WriteFloatOutputEvent extends WritingDataListener<Float> {
    @Override
    public void writeDataToServer(AsynchronousSocketChannel asyncServerSocket, Float value) {
        byte[] bytes = ByteBuffer.allocate(Float.BYTES).putFloat(value).array();
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        asyncServerSocket.write(buffer, null, new CompletionHandler<Integer, Void>() {
            @Override
            public void completed(Integer result, Void attachment) {
                buffer.clear();
                setWriting(false);
            }

            @Override
            public void failed(Throwable exc, Void attachment) {
                exc.printStackTrace();
            }
        });
    }
}
