package example.chat.server.packet.out;

import example.chat.common.api.ChatPacketWriter;
import example.chat.common.object.User;
import net.techtrends.general.listeners.ResponseCallback;
import net.techtrends.general.listeners.output.OutputListener;

public class SendMessageToUser implements ChatPacketWriter {


    private final User user;
    private final String rawMessage;

    public SendMessageToUser(User user, String rawMessage) {
        this.user = user;
        this.rawMessage = rawMessage;
    }

    @Override
    public void process(OutputListener outputListener) {
        String[] args = rawMessage.split("_");
        System.out.println(args[0] + ": " + args[1]);
        user.getOutputListener().sendString(args[0] + ": " + args[1], new ResponseCallback() {
            @Override
            public void complete(Object o) {
                System.out.println("Message sent to " + user.getName());
            }

            @Override
            public void completeExceptionally(Throwable throwable) {
                System.out.println("Something went wrong: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public int packetID() {
        return 2;
    }

    @Override
    public Object write() {
        return null;
    }
}
