package org.chiu.micro.search.config;

import org.chiu.micro.search.rpc.AuthHttpService;
import org.chiu.micro.search.rpc.BlogHttpService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.net.http.HttpClient;

@Configuration
@RequiredArgsConstructor
public class RpcConfig {

    private final HttpClient httpClient;
    
    private final HttpInterceptor httpInterceptor;

    @Bean
    BlogHttpService blogHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient client = RestClient.builder()
                .baseUrl("http://micro-blog:8081/inner")
                .requestFactory(requestFactory)
                .requestInterceptor(httpInterceptor)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(BlogHttpService.class);
    }

    @Bean
    AuthHttpService authHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient client = RestClient.builder()
                .baseUrl("http://micro-auth:8081/inner")
                .requestFactory(requestFactory)
                .requestInterceptor(httpInterceptor)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(AuthHttpService.class);
    }
}
