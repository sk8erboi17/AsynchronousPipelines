package example.client.request;

import net.techtrends.listeners.response.Callback;
import net.techtrends.listeners.response.CallbackBuilder;
import net.techtrends.network.pipeline.out.content.Request;

public class SayHelloToEmbeddedServer implements Request {
    private final String message;

    public SayHelloToEmbeddedServer(String message) {
        this.message = message;
    }

    @Override
    public Object getMessage() {
        return message;
    }

    @Override
    public Callback getCallback() {
        return new CallbackBuilder()
                .onComplete(null)
                .onException(Throwable::printStackTrace).build();
    }
}
