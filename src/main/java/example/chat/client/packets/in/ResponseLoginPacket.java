package example.chat.client.packets.in;

import example.chat.common.api.ChatPacketReader;
import example.chat.common.api.ChatPacketWriter;

public class ResponseLoginPacket implements ChatPacketReader {
    @Override
    public void read(ChatPacketWriter packet) {
        String response = (String) packet.write();
        System.out.println(response);
    }
}
