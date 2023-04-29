package example.chat.client.packets.out;

import example.chat.common.api.ChatPacketWriter;
import example.chat.common.object.User;
import net.techtrends.general.ObjectUtils;
import net.techtrends.general.listeners.ResponseCallback;
import net.techtrends.general.listeners.output.OutputListener;

import java.io.IOException;
import java.io.Serializable;

public class RequestConnectionPacket implements ChatPacketWriter, Serializable {

    private final String username;
    private final String sex;
    private final int age;

    public RequestConnectionPacket(String username, String sex, int age) {
        this.username = username;
        this.sex = sex;
        this.age = age;
    }

    @Override
    public void process(OutputListener outputListener) {
        try {
            outputListener.sendByteArray(ObjectUtils.objectToByteArray(this), new ResponseCallback() {
                @Override
                public void complete(Object o) {
                    System.out.println("Sent request");
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
        return 0;
    }

    @Override
    public Object write() {
        return new User(username, sex, age);
    }
}
