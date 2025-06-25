package example.client.request;

import it.sk8erboi17.listeners.response.Callback;
import it.sk8erboi17.listeners.response.CallbackBuilder;
import it.sk8erboi17.network.pipeline.out.content.Request;

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
