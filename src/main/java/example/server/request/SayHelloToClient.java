package example.server.request;

import net.techtrends.listeners.response.CallbackBuilder;
import net.techtrends.network.pipeline.out.Request;

public class SayHelloToClient extends Request {
    public SayHelloToClient() {
        super(new CallbackBuilder()
                        .onComplete(null)
                        .onException(Throwable::printStackTrace).build(),
                "Messaggio dal server: Ciao Client!");
    }

}
