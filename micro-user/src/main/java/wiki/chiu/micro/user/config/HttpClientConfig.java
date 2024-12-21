package wiki.chiu.micro.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.DefaultUriBuilderFactory;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.common.rpc.config.HttpInterceptor;
import wiki.chiu.micro.common.rpc.config.RpcClientFactory;

import java.net.http.HttpClient;
import java.util.concurrent.Executors;

@Configuration
public class HttpClientConfig {

    @Value("${megalith.blog.aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${megalith.blog.oss.endpoint}")
    private String ep;

    @Value("${megalith.blog.oss.base-url}")
    private String baseUrl;

    @Value("${megalith.blog.auth-url}")
    private String authUrl;

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
    AuthHttpService authHttpService() {
        return RpcClientFactory.createHttpService(AuthHttpService.class, authUrl, httpClient(), httpInterceptor(), null, null);
    }

    @Bean
    OssHttpService ossHttpService() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.HOST, bucketName + "." + ep);
        return RpcClientFactory.createHttpService(OssHttpService.class, baseUrl, httpClient(), null, DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, httpHeaders);
    }
}
