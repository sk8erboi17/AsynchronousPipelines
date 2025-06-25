package it.sk8erboi17.listeners.response.implementation;

import it.sk8erboi17.listeners.response.Callback;
import java.util.function.Consumer;

public class SimpleCallback implements Callback {
    // Consumer to be executed when the operation completes successfully.
    private final Consumer<Object> onComplete;

    // Consumer to be executed when the operation fails with an exception.
    private final Consumer<Throwable> onException;

    // Constructor that initializes the completion and exception handlers.
    public SimpleCallback(Consumer<Object> onComplete, Consumer<Throwable> onException) {
        this.onComplete = onComplete;
        this.onException = onException;
    }

    // Method invoked when the operation completes successfully.
    @Override
    public void complete(Object o) {
        if (onComplete != null) {
            onComplete.accept(o);
        }
    }

    // Method invoked when the operation fails with an exception.
    @Override
    public void completeExceptionally(Throwable throwable) {
        if (onException != null) {
            onException.accept(throwable);
        }
    }
}
