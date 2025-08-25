package it.sk8erboi17.listeners.response;

import java.util.function.Consumer;

/**
 * A builder class for creating instances of Callback.
 * It allows setting custom actions for completion and exceptions using method chaining.
 */
public class CallbackBuilder {
    // Consumer to be executed when the operation completes successfully.
    private Consumer<Object> onComplete;

    // Consumer to be executed when the operation fails with an exception.
    private Consumer<Throwable> onException;

    // Sets the onComplete action and returns the builder instance.
    public CallbackBuilder onComplete(Consumer<Object> onComplete) {
        this.onComplete = onComplete;
        return this;
    }

    // Sets the onException action and returns the builder instance.
    public CallbackBuilder onException(Consumer<Throwable> onException) {
        this.onException = onException;
        return this;
    }

    // Builds and returns an instance of SimpleCallback with the provided actions.
    public Callback build() {
        return new SimpleCallback(onComplete, onException);
    }
}
