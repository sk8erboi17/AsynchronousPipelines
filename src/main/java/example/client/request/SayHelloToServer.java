package example.client.request;

import net.techtrends.listeners.response.CallbackBuilder;
import net.techtrends.network.pipeline.out.Request;

public class SayHelloToServer extends Request {
    public SayHelloToServer(String message) {
        super(new CallbackBuilder()
                        .onComplete(null)
                        .onException(Throwable::printStackTrace).build(),
                message);
    }


}
