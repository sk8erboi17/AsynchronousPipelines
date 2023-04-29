package example.chat.common.api;

import net.techtrends.general.listeners.output.OutputListener;

public interface ChatPacketWriter {
    void process(OutputListener outputListener);

    int packetID();

    Object write();
}
