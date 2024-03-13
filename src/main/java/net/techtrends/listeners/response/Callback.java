package net.techtrends.listeners.response;

public interface Callback {

    void complete(Object o);

    void completeExceptionally(Throwable throwable);
    
}
