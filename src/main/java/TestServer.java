import net.techtrends.server.AsyncServerSocket;
import net.techtrends.server.Listener;
import net.techtrends.server.events.input.ReaderStringInputEvent;
import net.techtrends.server.events.output.WriteStringOutputEvent;
import net.techtrends.server.listeners.input.InputListener;
import net.techtrends.server.listeners.output.OutputListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class TestServer {
    public static void main(String[] args) {

        //Create Server
        AsynchronousServerSocketChannel serverSocketChannel;
        try {
            serverSocketChannel = AsyncServerSocket.createServer(new InetSocketAddress(8080));
            //Start Listen Clients
            Listener.getInstance().startConnectionListen(serverSocketChannel, socketChannel -> {
                System.out.println("Client Connected");
                ReaderStringInputEvent dataListener = new ReaderStringInputEvent();
                WriteStringOutputEvent writeStringOutputEvent = new WriteStringOutputEvent();
                new InputListener<String>().handle(socketChannel, dataListener, System.out::println);
                new OutputListener<String>().handle(socketChannel,"hello",writeStringOutputEvent);

            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
