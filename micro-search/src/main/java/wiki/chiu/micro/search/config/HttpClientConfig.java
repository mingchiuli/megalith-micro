package wiki.chiu.micro.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import wiki.chiu.micro.common.rpc.config.AuthHttpInterceptor;
import wiki.chiu.micro.common.rpc.config.RpcClientFactory;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;

@Configuration
public class HttpClientConfig {

    @Value("${megalith.blog.auth-url}")
    private String authUrl;

    @Value("${megalith.blog.blog-url}")
    private String blogUrl;

    @Bean
    HttpClient httpClient() {
        return HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())  // Configure to use virtual threads
                .build();
    }

    @Bean
    AuthHttpInterceptor httpInterceptor() {
        return new AuthHttpInterceptor();
    }

    @Bean
    AuthHttpService authHttpService() {
        return RpcClientFactory.createHttpService(AuthHttpService.class, authUrl, httpClient(), httpInterceptor(), null, null);
    }

    @Bean
    BlogHttpService blogHttpService() {
        return RpcClientFactory.createHttpService(BlogHttpService.class, blogUrl, httpClient(), httpInterceptor(), null, null);
    }
}
