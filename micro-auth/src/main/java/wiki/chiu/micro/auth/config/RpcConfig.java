package wiki.chiu.micro.auth.config;

import wiki.chiu.micro.common.rpc.SmsHttpService;
import wiki.chiu.micro.common.rpc.UserHttpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.http.HttpClient;
import java.time.Duration;


@Configuration
public class RpcConfig {

    private final HttpClient httpClient;

    private final HttpInterceptor httpInterceptor;

    @Value("${megalith.blog.sms.base-url}")
    private String baseUrl;

    public RpcConfig(HttpClient httpClient, HttpInterceptor httpInterceptor) {
        this.httpClient = httpClient;
        this.httpInterceptor = httpInterceptor;
    }

    @Bean
    SmsHttpService smsHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory(baseUrl);
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        RestClient client = RestClient.builder()
                .baseUrl(baseUrl)
                .uriBuilderFactory(uriBuilderFactory)
                .requestFactory(requestFactory)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(SmsHttpService.class);
    }


    @Bean
    UserHttpService userHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient client = RestClient.builder()
                .baseUrl("http://micro-user:8081/inner")
                .requestFactory(requestFactory)
                .requestInterceptor(httpInterceptor)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(UserHttpService.class);
    }

}
