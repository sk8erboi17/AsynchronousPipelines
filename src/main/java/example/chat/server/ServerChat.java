package example.chat.server;

import example.chat.common.api.ChatPacketWriter;
import example.chat.server.packet.in.LoginRequestFromUser;
import example.chat.server.packet.in.MessageFromUserPacket;
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

public class ServerChat {
    public static void main(String[] args) {

        try {
            AsynchronousServerSocketChannel serverSocketChannel = AsyncServerSocket.createServer(new InetSocketAddress(8080));

            Listener.getInstance().startConnectionListen(serverSocketChannel, socketChannel -> {
                System.out.println("Client Connected");
                InputListener inputListener = new InputListener(socketChannel, true, 1024, 4192, createInputListenerResponseCallback(socketChannel));
                inputListener.start();
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
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

                if (o instanceof byte[]) {
                    try {
                        ChatPacketWriter packetName = (ChatPacketWriter) ObjectUtils.byteArrayToObject((byte[]) o);
                        int id = packetName.packetID();
                        /* LOGIN */
                        if (id == 0) {
                            new LoginRequestFromUser(socketChannel).read(packetName);
                        }

                        /* MESSAGES */
                        if (id == 3) {
                            new MessageFromUserPacket().read(packetName);
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
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
