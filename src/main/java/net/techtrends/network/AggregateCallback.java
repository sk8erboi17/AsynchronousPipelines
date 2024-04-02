package net.techtrends.network;

import net.techtrends.listeners.response.Callback;

import java.util.Collection;

public class AggregateCallback implements Callback {
    private final Collection<Callback> callbacks;

    public AggregateCallback(Collection<Callback> callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public void complete(Object o) {
        for (Callback callback : callbacks) {
            callback.complete(o);
        }
    }

    @Override
    public void completeExceptionally(Throwable throwable) {
        for (Callback callback : callbacks) {
            callback.completeExceptionally(throwable);
        }
    }
}
