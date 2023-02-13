package net.techtrends.server.listeners.input;

import net.techtrends.server.Listener;
import net.techtrends.server.events.ResponseCallback;
import net.techtrends.server.events.input.ReadingDataListener;

import java.nio.channels.AsynchronousSocketChannel;

public class InputListener<T> implements InputEventHandler<T> {

    @Override
    public void handle(AsynchronousSocketChannel asyncServerSocket, ReadingDataListener<T> dataListener, ResponseCallback<T> callback) {
        Listener.getInstance().getExecutors().execute(() -> {
                if(callback == null) return;
                dataListener.readData(asyncServerSocket, callback);
        });
    }
}
