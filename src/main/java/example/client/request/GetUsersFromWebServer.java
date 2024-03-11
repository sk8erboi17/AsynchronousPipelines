package example.client.request;

import net.techtrends.network.pipeline.out.content.http.Http;
import net.techtrends.network.pipeline.out.content.http.HttpBuilder;

import java.net.http.HttpRequest;

public class GetUsersFromWebServer implements Http {
    @Override
    public HttpRequest request() {
        return new HttpBuilder().GET().uri("http://localhost:8080/api/users").build();
    }
}
