import net.techtrends.client.AsyncSocket;
import net.techtrends.client.socket.SocketThreadIO;
import net.techtrends.client.socket.output.SocketWriteThread;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.ExecutionException;

public class TestClient {


    public static void main(String[] args) {
        try {
            AsynchronousSocketChannel socketChannel = AsyncSocket.createClient(new InetSocketAddress("localhost",8080));
            SocketThreadIO socketReadThread= new net.techtrends.client.socket.input.SocketReadThread(socketChannel,1024);
            SocketThreadIO socketWriteThread = new SocketWriteThread(socketChannel,1024);
            socketWriteThread.startThread();
            socketReadThread.startThread();

        } catch (IOException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
