import net.techtrends.general.Listener;
import net.techtrends.general.listeners.input.InputListener;
import net.techtrends.server.AsyncServerSocket;

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
                InputListener listener2 = new InputListener();
                listener2.handle(socketChannel, System.out::println);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
