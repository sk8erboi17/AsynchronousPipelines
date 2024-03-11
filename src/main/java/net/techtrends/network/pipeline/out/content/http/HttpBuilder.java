package net.techtrends.network.pipeline.out.content.http;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class HttpBuilder {

    private URI uri;
    private String method = "GET"; // Default method
    private final Map<String, String> headers = new HashMap<>();
    private String body;
    private Duration timeout;

    public HttpBuilder uri(String uri) {
        this.uri = URI.create(uri);
        return this;
    }

    public HttpBuilder GET() {
        this.method = "GET";
        return this;
    }

    public HttpBuilder POST(String body) {
        this.method = "POST";
        this.body = body;
        return this;
    }

    public HttpBuilder PUT(String body) {
        this.method = "PUT";
        this.body = body;
        return this;
    }

    public HttpBuilder DELETE() {
        this.method = "DELETE";
        return this;
    }

    public HttpBuilder addHeader(String key, String value) {
        this.headers.put(key, value);
        return this;
    }

    public HttpBuilder timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    public HttpRequest build() {
        HttpRequest.Builder builder = HttpRequest.newBuilder();

        // Set the URI and method
        builder.uri(this.uri);
        switch (this.method) {
            case "POST":
                builder.POST(BodyPublishers.ofString(this.body));
                break;
            case "PUT":
                builder.PUT(BodyPublishers.ofString(this.body));
                break;
            case "DELETE":
                builder.DELETE();
                break;
            case "GET":
            default:
                builder.GET();
        }

        // Add the headers
        headers.forEach(builder::header);

        // Set timeout if present
        if (timeout != null) {
            builder.timeout(timeout);
        }

        return builder.build();
    }
}
