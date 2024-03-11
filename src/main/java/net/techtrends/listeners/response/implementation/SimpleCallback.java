package net.techtrends.listeners.response.implementation;

import net.techtrends.listeners.response.Callback;

import java.util.function.Consumer;

public class SimpleCallback implements Callback {
    private final Consumer<Object> onComplete;
    private final Consumer<Throwable> onException;

    public SimpleCallback(Consumer<Object> onComplete, Consumer<Throwable> onException) {
        this.onComplete = onComplete;
        this.onException = onException;
    }

    @Override
    public void complete(Object o) {
        if (onComplete != null) {
            onComplete.accept(o);
        }
    }

    @Override
    public void completeExceptionally(Throwable throwable) {
        if (onException != null) {
            onException.accept(throwable);
        }
    }
}
