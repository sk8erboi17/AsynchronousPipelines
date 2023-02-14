package net.techtrends.client.socket.output;

import net.techtrends.client.socket.SocketThreadIO;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

public class SocketWriteThread extends SocketThreadIO {
    public SocketWriteThread(AsynchronousSocketChannel socketChannel, int bytebuff){
        super(socketChannel, bytebuff);
    }


    @Override
    public void startThread() {
        Thread thread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String message = scanner.nextLine();
                buffer.clear();
                buffer.put(message.getBytes());
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
            }
        });
        thread.start();
    }
}
