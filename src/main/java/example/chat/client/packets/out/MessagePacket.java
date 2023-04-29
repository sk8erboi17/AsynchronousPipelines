package example.chat.client.packets.out;

import example.chat.common.api.ChatPacketWriter;
import net.techtrends.general.ObjectUtils;
import net.techtrends.general.listeners.ResponseCallback;
import net.techtrends.general.listeners.output.OutputListener;

import java.io.IOException;
import java.io.Serializable;

public class MessagePacket implements ChatPacketWriter, Serializable {
    private final String senderName;
    private final String message;

    public MessagePacket(String senderName, String message) {
        this.senderName = senderName;
        this.message = message;
    }

    @Override
    public void process(OutputListener outputListener) {
        try {
            outputListener.sendByteArray(ObjectUtils.objectToByteArray(this), new ResponseCallback() {
                @Override
                public void complete(Object o) {
                    //TODO: LOG MEX
                }

                @Override
                public void completeExceptionally(Throwable throwable) {
                    System.err.println("Something went wrong " + throwable.getMessage());
                    throwable.printStackTrace();
                }
            });
        } catch (IOException e) {
            System.err.println("Something went wrong " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public int packetID() {
        return 3;
    }

    @Override
    public Object write() {
        return senderName + "_" + message;
    }
}
