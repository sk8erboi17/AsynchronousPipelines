package net.techtrends.network;

import net.techtrends.listeners.response.Callback;
import java.util.Collection;

<<<<<<< HEAD
/**
 * This class aggregates multiple callbacks. When an operation completes,
 * AggregateCallback ensures that all registered callbacks are notified with the result or exception.
 */
public class AggregateCallback implements Callback {

    // A collection of callbacks that will be notified when an operation completes or fails.
=======
public class AggregateCallback {
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
    private final Collection<Callback> callbacks;

    public AggregateCallback(Collection<Callback> callbacks) {
        this.callbacks = callbacks;
    }

<<<<<<< HEAD
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
=======
    public Collection<Callback> getCallbacks() {
        return callbacks;
>>>>>>> c3733fd7991ada3ff017b98fb56adbb52dc8f6f9
    }
}
