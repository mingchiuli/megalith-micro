package wiki.chiu.micro.user.config;

import wiki.chiu.micro.common.rpc.OssHttpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import java.net.http.HttpClient;
import java.time.Duration;


@Configuration
public class RpcHttpConfig {

    @Value("${megalith.blog.aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${megalith.blog.oss.endpoint}")
    private String ep;

    @Value("${megalith.blog.oss.base-url}")
    private String baseUrl;

    private final HttpClient httpClient;

    public RpcHttpConfig(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Bean
    OssHttpService ossHttpService() {

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        RestClient client = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.HOST, bucketName + "." + ep)
                .build();

        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter)
                .build();
        return factory.createClient(OssHttpService.class);
    }
}
