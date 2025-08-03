package wiki.chiu.micro.blog.config;

import brave.Tracing;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.util.DefaultUriBuilderFactory;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import wiki.chiu.micro.common.rpc.UserHttpService;
import wiki.chiu.micro.common.rpc.config.interceptor.AuthHttpInterceptor;
import wiki.chiu.micro.common.rpc.config.RpcClientFactory;
import wiki.chiu.micro.common.rpc.config.interceptor.TraceHttpInterceptor;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;

@Configuration
public class HttpClientConfig {

    @Value("${megalith.blog.aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${megalith.blog.oss.endpoint}")
    private String ep;

    @Value("${megalith.blog.oss.base-url}")
    private String baseUrl;

    @Value("${megalith.blog.user-url}")
    private String userUrl;

    @Value("${megalith.blog.auth-url}")
    private String authUrl;

    @Value("${megalith.blog.search-url}")
    private String searchUrl;

    @Resource
    private Tracing tracing;

    @Bean
    ClientHttpRequestInterceptor tracingInterceptor() {
        return TraceHttpInterceptor.tracingInterceptor(tracing);
    }

    @Bean
    ClientHttpRequestInterceptor httpInterceptor() {
        return new AuthHttpInterceptor();
    }


    @Bean
    HttpClient httpClient() {
        return HttpClient.newBuilder()
                .executor(Executors.newVirtualThreadPerTaskExecutor())  // Configure to use virtual threads
                .build();
    }

    @Bean
    UserHttpService userHttpService() {
        return RpcClientFactory.createHttpService(UserHttpService.class, userUrl, httpClient(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, null, Duration.ofSeconds(10), List.of(tracingInterceptor(), httpInterceptor()));
    }

    @Bean
    OssHttpService ossHttpService() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.HOST, bucketName + "." + ep);
        return RpcClientFactory.createHttpService(OssHttpService.class, baseUrl, httpClient(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, httpHeaders, Duration.ofSeconds(10), null);
    }

    @Bean
    AuthHttpService authHttpService() {
        return RpcClientFactory.createHttpService(AuthHttpService.class, authUrl, httpClient(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, null, Duration.ofSeconds(10), List.of(tracingInterceptor(), httpInterceptor()));
    }

    @Bean
    SearchHttpService searchHttpService() {
        return RpcClientFactory.createHttpService(SearchHttpService.class, searchUrl, httpClient(), DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES, null, Duration.ofSeconds(10), List.of(tracingInterceptor(), httpInterceptor()));
    }
}
