package net.techtrends.network.pipeline.out.content.http;

import net.techtrends.listeners.response.Callback;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public interface Http {
    HttpRequest request();

    Callback response();
}
