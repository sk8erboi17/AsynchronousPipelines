package it.sk8erboi17.network.pipeline.out.content;

import it.sk8erboi17.listeners.response.Callback;

public interface Request {

    // Method to retrieve the message content from the request
    Object getMessage();

    // Method to retrieve the associated callback for the request
    Callback getCallback();

}
