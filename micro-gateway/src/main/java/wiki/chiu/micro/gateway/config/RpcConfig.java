package wiki.chiu.micro.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import wiki.chiu.micro.common.rpc.AuthHttpService;
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

    @Value("${megalith.blog.auth-url}")
    private String authUrl;

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

}
