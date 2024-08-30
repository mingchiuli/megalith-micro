package org.chiu.micro.exhibit.config;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClinet {

    @Bean
    HttpClient httpClient() {
        return HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())  // Configure to use virtual threads
                .build();
    }
}
