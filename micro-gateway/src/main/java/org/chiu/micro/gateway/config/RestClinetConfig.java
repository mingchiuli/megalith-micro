package org.chiu.micro.gateway.config;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class RestClinetConfig {

    private final HttpClient httpClient;

    @Bean
    RestClient restClient() {

        var requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
