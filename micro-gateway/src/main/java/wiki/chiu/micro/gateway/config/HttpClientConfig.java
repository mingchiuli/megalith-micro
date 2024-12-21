package wiki.chiu.micro.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.config.AuthHttpInterceptor;
import wiki.chiu.micro.common.rpc.config.RpcClientFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.Executors;

@Configuration
public class HttpClientConfig {

    @Value("${megalith.blog.auth-url}")
    private String authUrl;

    @Bean
    HttpClient httpClient() {
        return HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())// Configure to use virtual threads
                .build();
    }

    @Bean
    AuthHttpInterceptor interceptor() {
        return new AuthHttpInterceptor();
    }

    @Bean
    RestClient restClient() {

        var requestFactory = new JdkClientHttpRequestFactory(httpClient());
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .requestInterceptor(interceptor())
                .build();
    }

    @Bean
    AuthHttpService authHttpService() {
        return RpcClientFactory.createHttpService(AuthHttpService.class, authUrl, httpClient(), interceptor(), null, null);
    }
}
