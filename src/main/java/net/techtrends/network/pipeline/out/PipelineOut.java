package net.techtrends.network.pipeline.out;

import net.techtrends.listeners.output.OutputListener;
import net.techtrends.listeners.response.Callback;
import net.techtrends.network.pipeline.out.content.Request;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;

public class PipelineOut {
    private HttpClient httpClient;

    private AsynchronousSocketChannel client;

    private boolean allocateDirect;

    private int initBuffer;

    private boolean isHttpEnabled = false;


    public PipelineOut(AsynchronousSocketChannel client, boolean allocateDirect, int initBuffer) {
        this.client = client;
        this.allocateDirect = allocateDirect;
        this.initBuffer = initBuffer;
    }

    public PipelineOut() {
        this.isHttpEnabled = true;
        httpClient = HttpClient.newHttpClient();
    }

    public void registerRequest(Request request) {
        if (!isHttpEnabled) {
            handleNonHttpRequest(request);
        }
    }

    private void handleNonHttpRequest(Request request) {
        OutputListener outputListener = new OutputListener(client, initBuffer, allocateDirect);
        Object message = request.getMessage();
        switch (message) {
            case String s -> outputListener.sendString(s, request.getCallback());
            case Integer i -> outputListener.sendInt(i, request.getCallback());
            case Float v -> outputListener.sendFloat(v, request.getCallback());
            case Double v -> outputListener.sendDouble(v, request.getCallback());
            case Character c -> outputListener.sendChar(c, request.getCallback());
            case byte[] bytes -> outputListener.sendByteArray(bytes, request.getCallback());
            case null, default -> System.err.println("Unsupported message type: " + message.getClass().getSimpleName());
        }
    }

    private void sendHttpRequest(HttpRequest httpRequest, Callback responseCallback) {
        CompletableFuture<HttpResponse<String>> response = httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());

        response.thenApply(HttpResponse::body)
                .thenAccept(responseCallback::complete)
                .exceptionally(e -> {
                    responseCallback.completeExceptionally(e);
                    return null;
                });
    }

}
