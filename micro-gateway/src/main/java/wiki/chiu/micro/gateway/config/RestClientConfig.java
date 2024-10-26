package wiki.chiu.micro.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RestClientConfig {

    private final HttpClient httpClient;

    private final HttpInterceptor httpInterceptor;

    public RestClientConfig(HttpClient httpClient, HttpInterceptor httpInterceptor) {
        this.httpClient = httpClient;
        this.httpInterceptor = httpInterceptor;
    }

    @Bean
    RestClient restClient() {

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .requestInterceptor(httpInterceptor)
                .build();
    }
}
