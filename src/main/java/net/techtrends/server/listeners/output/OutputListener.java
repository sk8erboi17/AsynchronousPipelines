package net.techtrends.server.listeners.output;

import net.techtrends.server.Listener;
import net.techtrends.server.events.output.WritingDataListener;

import java.nio.channels.AsynchronousSocketChannel;

public class OutputListener<T> implements OutputEventHandler<T> {


    @Override
    public void handle(AsynchronousSocketChannel asyncServerSocket, T type, WritingDataListener<T> dataListener) {
        Listener.getInstance().getExecutors().execute(() -> dataListener.handleWrite(type, asyncServerSocket));
    }
}
