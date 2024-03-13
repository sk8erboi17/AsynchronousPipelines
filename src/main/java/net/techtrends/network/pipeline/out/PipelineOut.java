package net.techtrends.network.pipeline.out;

import net.techtrends.listeners.output.OutputListener;
import net.techtrends.network.pipeline.Pipeline;
import net.techtrends.network.pipeline.out.content.Request;
import net.techtrends.network.pipeline.out.content.http.HttpFormatter;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.CompletableFuture;

public class PipelineOut implements Pipeline {
    private final OutputListener outputListener;

    private final HttpClient httpClient;

    private final boolean isHttpEnabled;

    public PipelineOut(AsynchronousSocketChannel client, boolean allocateDirect, int initBuffer, boolean isHttpEnabled) {
        this.isHttpEnabled = isHttpEnabled;
        outputListener = new OutputListener(client, initBuffer, allocateDirect);
        httpClient = HttpClient.newHttpClient();
    }

    public void registerRequest(HttpRequest request) {
        if (isHttpEnabled) {
            sendHttpRequest(request);
        }
    }

    public void registerRequest(Request request) {
        if (!isHttpEnabled) {
            handleNonHttpRequest(request);
        }
    }

    private void handleNonHttpRequest(Request request) {
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

    private void sendHttpRequest(HttpRequest httpRequest) {
        CompletableFuture<HttpResponse<String>> response = httpClient.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofString());

        response.thenApply(HttpResponse::body).thenAccept(HttpFormatter::formatHttpResponse).exceptionally(e -> {
            e.printStackTrace();
            return null;
        });
    }

    @Override
    public void closePipeline() {
        outputListener.close();
    }
}
