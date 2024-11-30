package wiki.chiu.micro.exhibit.config;

import org.springframework.beans.factory.annotation.Value;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import wiki.chiu.micro.common.rpc.UserHttpService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class RpcConfig {

    private final HttpClient httpClient;

    private final HttpInterceptor httpInterceptor;

    public RpcConfig(HttpClient httpClient, HttpInterceptor httpInterceptor) {
        this.httpClient = httpClient;
        this.httpInterceptor = httpInterceptor;
    }

    @Value("${megalith.blog.user-url}")
    private String userUrl;

    @Value("${megalith.blog.auth-url}")
    private String authUrl;

    @Value("${megalith.blog.blog-url}")
    private String blogUrl;

    @Value("${megalith.blog.search-url}")
    private String searchUrl;

    @Bean
    BlogHttpService blogHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient client = RestClient.builder()
                .baseUrl(blogUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(httpInterceptor)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(BlogHttpService.class);
    }


    @Bean
    UserHttpService userHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient client = RestClient.builder()
                .baseUrl(userUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(httpInterceptor)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(UserHttpService.class);
    }

    @Bean
    AuthHttpService authHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient client = RestClient.builder()
                .baseUrl(authUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(httpInterceptor)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(AuthHttpService.class);
    }

    @Bean
    SearchHttpService searchHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient client = RestClient.builder()
                .baseUrl(searchUrl)
                .requestFactory(requestFactory)
                .requestInterceptor(httpInterceptor)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(SearchHttpService.class);
    }
}
