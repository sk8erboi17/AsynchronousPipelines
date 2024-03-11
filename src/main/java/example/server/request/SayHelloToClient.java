package example.server.request;

import net.techtrends.listeners.response.CallbackBuilder;
import net.techtrends.network.pipeline.out.Request;

public class SayHelloToClient extends Request {
    public SayHelloToClient(String message) {
        super(new CallbackBuilder()
                        .onComplete(null)
                        .onException(Throwable::printStackTrace).build(),
                message);
    }

}
