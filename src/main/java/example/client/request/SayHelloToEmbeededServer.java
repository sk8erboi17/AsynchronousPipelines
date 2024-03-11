package example.client.request;

import net.techtrends.listeners.response.CallbackBuilder;
import net.techtrends.network.pipeline.out.content.Request;

public class SayHelloToEmbeededServer extends Request {
    public SayHelloToEmbeededServer(String message) {
        super(new CallbackBuilder()
                        .onComplete(null)
                        .onException(Throwable::printStackTrace).build(),
                message);
    }

}
