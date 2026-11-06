package wiki.chiu.micro.blog.adapter.out.http;

import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import wiki.chiu.micro.blog.application.port.out.CollaborationRoomInitializer;

@Component
public class SyncRoomInitializer implements CollaborationRoomInitializer {
    private final RestClient client;

    public SyncRoomInitializer(RestClient.Builder builder,
        @Value("${megalith.blog.sync-url:http://127.0.0.1:8089}") String syncUrl) {
        var factory = new JdkClientHttpRequestFactory(
            java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.client = builder.baseUrl(syncUrl).requestFactory(factory).build();
    }

    @Override
    public void initialize(String roomId, String content) {
        client.post().uri("/inner/rooms/{roomId}/initialize", roomId)
            .body(Map.of("content", content)).retrieve().toBodilessEntity();
    }
}
