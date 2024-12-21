package wiki.chiu.micro.exhibit.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.DefaultUriBuilderFactory;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import wiki.chiu.micro.common.rpc.UserHttpService;
import wiki.chiu.micro.common.rpc.config.HttpInterceptor;
import wiki.chiu.micro.common.rpc.config.RpcClientFactory;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;

@Configuration
public class HttpClientConfig {

    @Value("${megalith.blog.user-url}")
    private String userUrl;

    @Value("${megalith.blog.auth-url}")
    private String authUrl;

    @Value("${megalith.blog.blog-url}")
    private String blogUrl;

    @Value("${megalith.blog.search-url}")
    private String searchUrl;

    @Bean
    HttpClient httpClient() {
        return HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())  // Configure to use virtual threads
                .build();
    }

    @Bean
    HttpInterceptor httpInterceptor() {
        return new HttpInterceptor();
    }

    @Bean
    UserHttpService userHttpService() {
        return RpcClientFactory.createHttpService(UserHttpService.class, userUrl, httpClient(), httpInterceptor(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, null);
    }

    @Bean
    BlogHttpService blogHttpService() {
        return RpcClientFactory.createHttpService(BlogHttpService.class, blogUrl, httpClient(), httpInterceptor(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, null);
    }

    @Bean
    AuthHttpService authHttpService() {
        return RpcClientFactory.createHttpService(AuthHttpService.class, authUrl, httpClient(), httpInterceptor(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, null);
    }

    @Bean
    SearchHttpService searchHttpService() {
        return RpcClientFactory.createHttpService(SearchHttpService.class, searchUrl, httpClient(), httpInterceptor(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, null);
    }

}
