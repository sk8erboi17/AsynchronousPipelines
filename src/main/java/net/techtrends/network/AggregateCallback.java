package net.techtrends.network;

import net.techtrends.listeners.response.Callback;

import java.util.Collection;

public class AggregateCallback {
    private final Collection<Callback> callbacks;

    public AggregateCallback(Collection<Callback> callbacks) {
        this.callbacks = callbacks;
    }

    public Collection<Callback> getCallbacks() {
        return callbacks;
    }
}
