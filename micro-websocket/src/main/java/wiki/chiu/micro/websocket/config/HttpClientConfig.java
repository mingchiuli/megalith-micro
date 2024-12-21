package wiki.chiu.micro.websocket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.config.RpcClientFactory;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;

@Configuration
public class HttpClientConfig {

    @Value("${megalith.blog.auth-url}")
    private String authUrl;


    @Bean
    HttpClient httpClient() {
        return HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())  // Configure to use virtual threads
                .build();
    }

    @Bean
    AuthHttpService authHttpService() {
        return RpcClientFactory.createHttpService(AuthHttpService.class, authUrl, httpClient(), null, null, null);
    }
}
