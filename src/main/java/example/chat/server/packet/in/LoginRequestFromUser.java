package example.chat.server.packet.in;

import example.chat.common.api.ChatPacketReader;
import example.chat.common.api.ChatPacketWriter;
import example.chat.common.object.User;
import example.chat.server.container.UserContainer;
import example.chat.server.packet.out.ResponseConnectionPacket;
import net.techtrends.general.listeners.output.OutputListener;

import java.nio.channels.AsynchronousSocketChannel;

public class LoginRequestFromUser implements ChatPacketReader {
    private final AsynchronousSocketChannel socketChannel;

    public LoginRequestFromUser(AsynchronousSocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    @Override
    public void read(ChatPacketWriter packet) {
        User userInstance = (User) packet.write();
        OutputListener newOutputListenerUser = new OutputListener(socketChannel, 1024, true);
        userInstance.setOutputListener(newOutputListenerUser);
        if (UserContainer.getUser(userInstance.getName()) == null) {
            UserContainer.addUser(userInstance);
            new ResponseConnectionPacket("Server: User not found, added to db").process(newOutputListenerUser);
            System.out.println("Added user");
        } else {
            new ResponseConnectionPacket("Server: User found,loaded from db").process(newOutputListenerUser);
            System.out.println("Load user");
        }
    }
}
