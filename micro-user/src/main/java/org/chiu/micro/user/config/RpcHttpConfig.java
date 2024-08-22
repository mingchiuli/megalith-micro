package org.chiu.micro.user.config;

import org.chiu.micro.user.http.OssHttpService;
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
public class RpcHttpConfig {

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
}
