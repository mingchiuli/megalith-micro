package org.chiu.micro.blog.config;

import org.chiu.micro.blog.rpc.AuthHttpService;
import org.chiu.micro.blog.rpc.OssHttpService;
import org.chiu.micro.blog.rpc.UserHttpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.time.Duration;
import java.net.http.HttpClient;
import java.util.concurrent.Executors;


@Configuration
public class RpcConfig {

    @Value("${blog.aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${blog.oss.endpoint}")
    private String ep;

    @Value("${blog.oss.base-url}")
    private String baseUrl;

    @Bean
    OssHttpService ossHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())  // Configure to use virtual threads
                .build());
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.HOST, bucketName + "." + ep)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(OssHttpService.class);
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
}
