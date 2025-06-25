package it.sk8erboi17.network;

import it.sk8erboi17.listeners.response.Callback;
import java.util.Collection;

/**
 * This class aggregates multiple callbacks. When an operation completes,
 * AggregateCallback ensures that all registered callbacks are notified with the result or exception.
 */
public class AggregateCallback implements Callback {

    // A collection of callbacks that will be notified when an operation completes or fails.
    private final Collection<Callback> callbacks;

    public AggregateCallback(Collection<Callback> callbacks) {
        this.callbacks = callbacks;
    }

    // Method called when the operation completes successfully.
    @Override
    public void complete(Object o) {
        // Notify all callbacks in the collection.
        for (Callback callback : callbacks) {
            callback.complete(o);
        }
    }

    // Method called when the operation fails with an exception.
    @Override
    public void completeExceptionally(Throwable throwable) {
        // Notify all callbacks in the collection.
        for (Callback callback : callbacks) {
            callback.completeExceptionally(throwable);
        }
    }
}
