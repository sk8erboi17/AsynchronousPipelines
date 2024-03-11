package net.techtrends.network.pipeline.out;

import net.techtrends.listeners.response.Callback;

public class Request {
    private final Object message;

    private final Callback callback;

    public Request(Callback callback, Object message) {
        this.callback = callback;
        this.message = message;
    }

    public Object getMessage() {
        return message;
    }

    public Callback getCallback() {
        return callback;
    }

}
