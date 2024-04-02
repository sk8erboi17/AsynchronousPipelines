package example.client.request;

import net.techtrends.listeners.response.Callback;
import net.techtrends.listeners.response.CallbackBuilder;
import net.techtrends.network.pipeline.out.content.http.Http;
import net.techtrends.network.pipeline.out.content.http.HttpBuilder;
import net.techtrends.network.pipeline.out.content.http.HttpFormatter;

import java.net.http.HttpRequest;

public class GetUsersFromWebServer implements Http {
    @Override
    public HttpRequest request() {
        return new HttpBuilder().GET().uri("http://136.243.124.131").build();
    }

    @Override
    public Callback response() {
        return new CallbackBuilder()
                .onComplete(response -> HttpFormatter.formatHttpResponse(String.valueOf(response)))
                .onException(exception -> {
                    throw new RuntimeException("An error occurred while receiving the response " + exception.getMessage(), exception);
                }).build();
    }

}
