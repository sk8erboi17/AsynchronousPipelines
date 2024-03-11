package example.client.request;

import net.techtrends.listeners.response.CallbackBuilder;
import net.techtrends.network.pipeline.out.Request;

public class SayHelloToServer extends Request {
    public SayHelloToServer() {
        super(new CallbackBuilder()
                        .onComplete(null)
                        .onException(Throwable::printStackTrace).build()
                , "Message from client: Hi server!");
    }


}
