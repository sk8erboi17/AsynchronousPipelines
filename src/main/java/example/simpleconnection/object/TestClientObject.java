package example.simpleconnection.object;

import net.techtrends.client.AsyncSocket;
import net.techtrends.general.ObjectUtils;
import net.techtrends.general.listeners.ResponseCallback;
import net.techtrends.general.listeners.output.OutputListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.UUID;

/**
 * This class is an example of a client that sends a serialized object over a network using an AsyncSocket.
 * The User object is created with a UUID and an age value, and then serialized using the ObjectUtils class.
 * The output listener sends the serialized object as a byte array to the server, and a callback function is used to handle the response of the send operation.
 * If the send operation is successful, a success message is printed, otherwise an error message is printed with the cause of the exception.
 */
public class TestClientObject {

    public static void main(String[] args) {
        try {
            AsynchronousSocketChannel socketChannel = AsyncSocket.createClient(new InetSocketAddress("localhost", 8080));

            OutputListener listener = new OutputListener(socketChannel, 2048, true);
            Human human = new Human(UUID.randomUUID(), 2);
            listener.sendByteArray(ObjectUtils.objectToByteArray(human), new ResponseCallback() {
                @Override
                public void complete(Object o) {
                    System.out.println("Data sent successfully");
                }

                @Override
                public void completeExceptionally(Throwable throwable) {
                    System.err.println("Error while sending data: " + throwable.getMessage());
                }
            });
            Thread.sleep(5000);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

