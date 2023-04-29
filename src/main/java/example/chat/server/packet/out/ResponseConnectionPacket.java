package example.chat.server.packet.out;

import example.chat.common.api.ChatPacketWriter;
import net.techtrends.general.ObjectUtils;
import net.techtrends.general.listeners.ResponseCallback;
import net.techtrends.general.listeners.output.OutputListener;

import java.io.IOException;
import java.io.Serializable;

public class ResponseConnectionPacket implements ChatPacketWriter, Serializable {


    private final String result;

    public ResponseConnectionPacket(String result) {
        this.result = result;
    }

    @Override
    public void process(OutputListener outputListener) {
        try {
            outputListener.sendByteArray(ObjectUtils.objectToByteArray(this), new ResponseCallback() {
                @Override
                public void complete(Object o) {
                    System.out.println("Response sent");
                }

                @Override
                public void completeExceptionally(Throwable throwable) {
                    System.out.println("Something went wrong: " + throwable.getMessage());
                    throwable.printStackTrace();
                }
            });
        } catch (IOException e) {
            System.out.println("Something went wrong: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public int packetID() {
        return 1;
    }

    @Override
    public Object write() {
        return result;
    }
}
