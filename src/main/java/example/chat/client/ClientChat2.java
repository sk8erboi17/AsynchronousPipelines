package example.chat.client;

import example.chat.client.packets.in.ResponseLoginPacket;
import example.chat.client.packets.out.MessagePacket;
import example.chat.client.packets.out.RequestConnectionPacket;
import example.chat.common.api.ChatPacketWriter;
import net.techtrends.client.AsyncSocket;
import net.techtrends.general.ObjectUtils;
import net.techtrends.general.listeners.ResponseCallback;
import net.techtrends.general.listeners.input.InputListener;
import net.techtrends.general.listeners.output.OutputListener;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.Scanner;

public class ClientChat2 {
    public static void main(String[] args) throws InterruptedException {
        AsynchronousSocketChannel socketChannel = AsyncSocket.createClient(new InetSocketAddress("localhost", 8080));

        ResponseCallback responseCallback = createInputListenerResponseCallback(socketChannel);
        InputListener inputListener = new InputListener(socketChannel, true, 1024, 4192, responseCallback);
        inputListener.start();

        OutputListener outputListener = new OutputListener(socketChannel, 1024 * 1024, true);
        Thread.sleep(400L);
        new RequestConnectionPacket("Testo", "M", 16).process(outputListener);
        Thread.sleep(2000L);

        Scanner scanner = new Scanner(System.in);
        System.out.println("Insert message here: ");
        while (true) {
            String message = scanner.nextLine();
            MessagePacket messagePacket = new MessagePacket("Testo", message);
            messagePacket.process(outputListener);
        }
    }


    private static ResponseCallback createInputListenerResponseCallback(AsynchronousSocketChannel socketChannel) {
        return new ResponseCallback() {
            @Override
            public void complete(Object o) {
                if (o instanceof String) {
                    System.out.println(o);
                }
                ChatPacketWriter packetName;
                try {
                    packetName = (ChatPacketWriter) ObjectUtils.byteArrayToObject((byte[]) o);
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                int id = packetName.packetID();
                getPacketProcessorById(packetName, id);
            }

            @Override
            public void completeExceptionally(Throwable throwable) {
                System.err.println("Error while receiving input: " + throwable.getMessage());
                AsyncSocket.closeSocketChannel(socketChannel);
            }
        };
    }


    private static void getPacketProcessorById(ChatPacketWriter chatPacketWriter, int id) {
        if (id == 1) {
            new ResponseLoginPacket().read(chatPacketWriter);
        }
    }
}

