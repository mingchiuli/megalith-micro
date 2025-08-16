package wiki.chiu.micro.search.config;

import brave.spring.web.TracingClientHttpRequestInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import wiki.chiu.micro.common.rpc.config.interceptor.AuthHttpInterceptor;
import wiki.chiu.micro.common.rpc.RpcClientFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;

@Configuration
public class HttpClientConfig {

    @Value("${megalith.blog.auth-url}")
    private String authUrl;

    @Value("${megalith.blog.blog-url}")
    private String blogUrl;

    @Resource
    private TracingClientHttpRequestInterceptor tracingInterceptor;

    @Resource
    private AuthHttpInterceptor authHttpInterceptor;

    @Bean
    HttpClient httpClient() {
        return HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())  // Configure to use virtual threads
                .build();
    }

    @Bean
    AuthHttpService authHttpService() {
        return RpcClientFactory.createHttpService(AuthHttpService.class, authUrl, httpClient(), null, null, Duration.ofSeconds(10), List.of(tracingInterceptor, authHttpInterceptor));
    }

    @Bean
    BlogHttpService blogHttpService() {
        return RpcClientFactory.createHttpService(BlogHttpService.class, blogUrl, httpClient(), null, null, Duration.ofSeconds(10), List.of(tracingInterceptor, authHttpInterceptor));
    }
}
