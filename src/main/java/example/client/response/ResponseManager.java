package example.client.response;

import net.techtrends.listeners.response.Callback;
import net.techtrends.listeners.response.CallbackBuilder;

public class ResponseManager {
    public static final Callback responseToServer = new CallbackBuilder()
            .onComplete(o -> {
                System.out.println("Response #1 :" + o);
            })
            .onException(Throwable::printStackTrace)
            .build();


    public static final Callback secondResponse = new CallbackBuilder()
            .onComplete(o -> {
                System.out.println("Response #2 :" + o);
            })
            .onException(Throwable::printStackTrace)
            .build();


}
