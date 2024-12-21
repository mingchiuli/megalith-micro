package wiki.chiu.micro.auth.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.util.DefaultUriBuilderFactory;
import wiki.chiu.micro.common.rpc.SmsHttpService;
import wiki.chiu.micro.common.rpc.UserHttpService;
import wiki.chiu.micro.common.rpc.config.AuthHttpInterceptor;
import wiki.chiu.micro.common.rpc.config.RpcClientFactory;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;

@Configuration
public class HttpClientConfig {

    @Value("${megalith.blog.sms.base-url}")
    private String baseUrl;

    @Value("${megalith.blog.user-url}")
    private String userUrl;

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
    SmsHttpService smsHttpService() {
        return RpcClientFactory.createHttpService(SmsHttpService.class, baseUrl, httpClient(), null, DefaultUriBuilderFactory.EncodingMode.NONE, null);
    }

    @Bean
    UserHttpService userHttpService() {
        return RpcClientFactory.createHttpService(UserHttpService.class, userUrl, httpClient(), httpInterceptor(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, null);
    }
}