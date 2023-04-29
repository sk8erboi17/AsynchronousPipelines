package example.chat.server.packet.in;

import example.chat.common.api.ChatPacketReader;
import example.chat.common.api.ChatPacketWriter;
import example.chat.common.object.User;
import example.chat.server.container.UserContainer;
import example.chat.server.packet.out.SendMessageToUser;

public class MessageFromUserPacket implements ChatPacketReader {
    @Override
    public void read(ChatPacketWriter packet) {
        System.out.println("Raw Message: " + packet.write());

        for (User user : UserContainer.getAllUsers()) {
            if (((String) packet.write()).split("_")[0].equalsIgnoreCase(user.getName())) continue;
            new SendMessageToUser(user, (String) packet.write()).process(user.getOutputListener());
        }

    }
}
