package org.chiu.micro.gateway.config;

import java.time.Duration;

import org.chiu.micro.gateway.rpc.AuthHttpService;
import org.chiu.micro.gateway.rpc.UserHttpService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.util.concurrent.Executors;
import java.net.http.HttpClient;


@Configuration
public class RpcConfig {

    @Bean
    AuthHttpService authHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())  // Configure to use virtual threads
                .build());
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient client = RestClient.builder()
                .baseUrl("http://micro-auth:8081/inner")
                .requestFactory(requestFactory)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(AuthHttpService.class);
    }

    @Bean
    UserHttpService userHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())  // Configure to use virtual threads
                .build());
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient client = RestClient.builder()
                .baseUrl("http://micro-user:8081/inner")
                .requestFactory(requestFactory)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(UserHttpService.class);
    }
}
