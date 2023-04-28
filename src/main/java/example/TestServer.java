package example;

import example.object.User;
import net.techtrends.client.AsyncSocket;
import net.techtrends.general.Listener;
import net.techtrends.general.ObjectUtils;
import net.techtrends.general.listeners.ResponseCallback;
import net.techtrends.general.listeners.input.InputListener;
import net.techtrends.server.AsyncServerSocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;

/**
 * This Java class sets up a server that listens for incoming connections and handles them by creating an input listener
 * that decodes received objects into User objects using ObjectUtils.byteArrayToObject() or print other value.
 * <p>
 * The main method sets up the server and starts listening for incoming connections.
 * It creates an input listener for each new connection and starts it.
 * The input listener uses a ResponseCallback to handle successful or failed input reception.
 * <p>
 * The createInputListenerResponseCallback() method returns a ResponseCallback that handles the reception of User objects by decoding them and printing their age and UUID.
 * In case of any exceptions, the method prints the error message and closes the socket connection using the AsyncSocket.closeSocketChannel() method.
 */
public class TestServer {
    public static void main(String[] args) {

        try {
            AsynchronousServerSocketChannel serverSocketChannel = AsyncServerSocket.createServer(new InetSocketAddress(8080));

            Listener.getInstance().startConnectionListen(serverSocketChannel, socketChannel -> {
                System.out.println("Client Connected");
                InputListener inputListener = new InputListener(socketChannel, true, createInputListenerResponseCallback(socketChannel));
                inputListener.start();
                Runtime.getRuntime().addShutdownHook(new Thread(() ->{
                    System.out.println("Server shutdown");
                    inputListener.close();
                }));
            });
        } catch (IOException e) {
            System.err.println("Error while starting server: " + e.getMessage());
        }
    }

    private static ResponseCallback createInputListenerResponseCallback(AsynchronousSocketChannel socketChannel) {
        return new ResponseCallback() {
            @Override
            public void complete(Object o) {
                try {
                    if (o instanceof byte[] object) {
                        User user = (User) ObjectUtils.byteArrayToObject(object);
                        System.out.println("User detected!");
                        System.out.println("AGE: " + user.getAge());
                        System.out.println("UUID: " + user.getUuid().toString());
                    }else{
                        System.out.println(o);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Error while decoding object: " + e.getMessage());
                    AsyncSocket.closeSocketChannel(socketChannel);
                }

            }

            @Override
            public void completeExceptionally(Throwable throwable) {
                System.err.println("Error while receiving input: " + throwable.getMessage());
                AsyncSocket.closeSocketChannel(socketChannel);
            }
        };
    }
}
