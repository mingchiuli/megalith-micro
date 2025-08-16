package wiki.chiu.micro.auth.config;

import brave.spring.web.TracingClientHttpRequestInterceptor;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.DefaultUriBuilderFactory;
import wiki.chiu.micro.common.rpc.AuthorityHttpService;
import wiki.chiu.micro.common.rpc.MenuHttpService;
import wiki.chiu.micro.common.rpc.SmsHttpService;
import wiki.chiu.micro.common.rpc.UserHttpService;
import wiki.chiu.micro.common.rpc.config.interceptor.AuthHttpInterceptor;
import wiki.chiu.micro.common.rpc.RpcClientFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;

@Configuration
public class HttpClientConfig {

    @Value("${megalith.blog.sms.base-url}")
    private String baseUrl;

    @Value("${megalith.blog.user-url}")
    private String userUrl;

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
    SmsHttpService smsHttpService() {
        return RpcClientFactory.createHttpService(SmsHttpService.class, baseUrl, httpClient(), DefaultUriBuilderFactory.EncodingMode.NONE, null, Duration.ofSeconds(20), null);
    }

    @Bean
    UserHttpService userHttpService() {
        return RpcClientFactory.createHttpService(UserHttpService.class, userUrl, httpClient(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, null, Duration.ofSeconds(10), List.of(tracingInterceptor, authHttpInterceptor));
    }

    @Bean
    MenuHttpService menuHttpService() {
        return RpcClientFactory.createHttpService(MenuHttpService.class, userUrl, httpClient(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, null, Duration.ofSeconds(10), List.of(tracingInterceptor, authHttpInterceptor));
    }

    @Bean
    AuthorityHttpService authorityHttpService() {
        return RpcClientFactory.createHttpService(AuthorityHttpService.class, userUrl, httpClient(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, null, Duration.ofSeconds(10), List.of(tracingInterceptor, authHttpInterceptor));
    }
}