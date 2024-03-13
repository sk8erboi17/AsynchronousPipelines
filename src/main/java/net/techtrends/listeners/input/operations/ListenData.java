package net.techtrends.listeners.input.operations;

import net.techtrends.listeners.response.Callback;
import net.techtrends.network.pipeline.out.content.http.HttpFormatter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ListenData {

    public void listen(ByteBuffer buffer, Callback callback) {
        if (buffer.remaining() <= 0) {
            return;
        }
        byte marker = buffer.get();
        System.out.println(marker);
        if (marker >= 0x01 && marker <= 0x06) {

            switch (marker) {
                case 0x01 -> handleString(buffer, callback);
                case 0x02 -> handleInt(buffer, callback);
                case 0x03 -> handleFloat(buffer, callback);
                case 0x04 -> handleDouble(buffer, callback);
                case 0x05 -> handleChar(buffer, callback);
                case 0x06 -> handleByteArray(buffer, callback);
            }
        } else {
            handleHTTP(buffer,callback);
        }
    }


    private void handleString(ByteBuffer buffer, Callback callback) {
        String data = StandardCharsets.UTF_8.decode(buffer).toString();
        callback.complete(data);
    }

    private void handleHTTP(ByteBuffer buffer, Callback callback) {
        String data = StandardCharsets.UTF_8.decode(buffer).toString();
        HttpFormatter.formatHttpResponse(data);
        callback.complete(data);
    }

    private void handleInt(ByteBuffer buffer, Callback callback) {
        int data = buffer.getInt();
        callback.complete(data);
    }

    private void handleFloat(ByteBuffer buffer, Callback callback) {
        float data = buffer.getFloat();
        callback.complete(data);
    }

    private void handleDouble(ByteBuffer buffer, Callback callback) {
        double data = buffer.getDouble();
        callback.complete(data);
    }

    private void handleChar(ByteBuffer buffer, Callback callback) {
        char data = buffer.getChar();
        callback.complete(data);
    }

    private void handleByteArray(ByteBuffer buffer, Callback callback) {
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        callback.complete(data);
    }

}
