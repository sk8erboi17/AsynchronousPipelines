package example.server.request;

import net.techtrends.listeners.response.Callback;
import net.techtrends.listeners.response.CallbackBuilder;
import net.techtrends.network.pipeline.out.content.Request;

public class SayHelloToClient implements Request {
    private final String message;

    public SayHelloToClient(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Callback getCallback() {
        return new CallbackBuilder()
                .onComplete(null)
                .onException(Throwable::printStackTrace)
                .build();
    }
}
