package net.techtrends.network.pipeline.out.content;

import net.techtrends.listeners.response.Callback;

public interface Request {

    Object getMessage();

    Callback getCallback();

}
