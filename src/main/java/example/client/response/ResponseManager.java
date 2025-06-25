package example.client.response;

import it.sk8erboi17.listeners.response.Callback;
import it.sk8erboi17.listeners.response.CallbackBuilder;

public class ResponseManager {
    public static final Callback responseToServer = new CallbackBuilder()
            .onComplete(o -> System.out.println("Response #1 :" + o))
            .onException(Throwable::printStackTrace)
            .build();


    public static final Callback secondResponse = new CallbackBuilder()
            .onComplete(o -> System.out.println("Response #2 :" + o))
            .onException(Throwable::printStackTrace)
            .build();


}
