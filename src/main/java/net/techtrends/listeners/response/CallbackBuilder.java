package net.techtrends.listeners.response;

import net.techtrends.listeners.response.implementation.SimpleCallback;

import java.util.function.Consumer;

public class CallbackBuilder {
    private Consumer<Object> onComplete;
    private Consumer<Throwable> onException;

    public CallbackBuilder onComplete(Consumer<Object> onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    public CallbackBuilder onException(Consumer<Throwable> onException) {
        this.onException = onException;
        return this;
    }

    public Callback build() {
        return new SimpleCallback(onComplete, onException);
    }
}
