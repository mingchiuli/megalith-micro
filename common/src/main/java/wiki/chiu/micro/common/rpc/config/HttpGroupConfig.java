package wiki.chiu.micro.common.rpc.config;

import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.StringUtils;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.util.DefaultUriBuilderFactory;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.config.interceptor.AuthHttpInterceptor;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@AutoConfiguration
public class HttpGroupConfig {

    @Value("${megalith.blog.aliyun.oss.bucket-name:}")
    private String bucketName;

    @Value("${megalith.blog.oss.endpoint:}")
    private String ep;

    @Value("${megalith.blog.http.user-timeout:10000}")
    private int userTimeout;

    @Value("${megalith.blog.http.oss-timeout:300000}")
    private int ossTimeout;

    @Value("${megalith.blog.http.auth-timeout:10000}")
    private int authTimeout;

    @Value("${megalith.blog.http.sms-timeout:10000}")
    private int smsTimeout;

    @Value("${megalith.blog.http.search-timeout:10000}")
    private int searchTimeout;

    @Value("${megalith.blog.http.blog-timeout:10000}")
    private int blogTimeout;

    @Value("${megalith.blog.user-url:http://127.0.0.1:8086/inner}")
    private String userUrl;

    @Value("${megalith.blog.auth-url:http://127.0.0.1:8081/inner}")
    private String authUrl;

    @Value("${megalith.blog.search-url:http://127.0.0.1:8085/inner}")
    private String searchUrl;

    @Value("${megalith.blog.blog-url:http://127.0.0.1:8082/inner}")
    private String blogUrl;

    @Value("${megalith.blog.oss.base-url:}")
    private String ossUrl;

    @Value("${megalith.blog.sms.base-url:}")
    private String smsUrl;

    @Bean
    AuthHttpInterceptor tokenInterceptor() {
        return new AuthHttpInterceptor();
    }

    @Bean
    RestClientHttpServiceGroupConfigurer groupConfigurer(AuthHttpInterceptor authHttpInterceptor, JsonMapper jsonMapper, ClientHttpRequestFactoryBuilder<?> requestFactoryBuilder, ObservationRegistry observationRegistry) {
        DefaultUriBuilderFactory userUriBuilderFactory = new DefaultUriBuilderFactory(userUrl);
        userUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES);

        DefaultUriBuilderFactory authUriBuilderFactory = new DefaultUriBuilderFactory(authUrl);
        authUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES);

        DefaultUriBuilderFactory searchUriBuilderFactory = new DefaultUriBuilderFactory(searchUrl);
        searchUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES);

        DefaultUriBuilderFactory blogUriBuilderFactory = new DefaultUriBuilderFactory(blogUrl);
        blogUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES);

        DefaultUriBuilderFactory ossUriBuilderFactory = new DefaultUriBuilderFactory(ossUrl);
        ossUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES);

        DefaultUriBuilderFactory smsUriBuilderFactory = new DefaultUriBuilderFactory(smsUrl);
        smsUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);


        return groups -> {
            // User 组配置 - 添加追踪和认证拦截器
            groups.filterByName("user").forEachClient((_, builder) ->
                    builder
                            .baseUrl(userUrl)
                            .uriBuilderFactory(userUriBuilderFactory)
                            .observationRegistry(observationRegistry)
                            .requestInterceptors(interceptors ->
                                    interceptors.add(authHttpInterceptor)
                            )
                            .requestFactory(requestFactoryBuilder.build(HttpClientSettings.defaults()
                                    .withReadTimeout(Duration.ofMillis(userTimeout))))
                            .defaultStatusHandler(HttpStatusCode::isError, (_, response) -> {
                                String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                                Result<?> result = jsonMapper.readValue(responseBody, Result.class);
                                throw new MissException(result.msg());
                            })
            );

            // OSS 组配置 - 添加自定义 Host 头
            if (StringUtils.hasLength(ossUrl)) {
                groups.filterByName("oss").forEachClient((_, builder) ->
                        builder
                                .baseUrl(ossUrl)
                                .uriBuilderFactory(ossUriBuilderFactory)
                                .observationRegistry(observationRegistry)
                                .defaultHeaders(headers ->
                                        headers.add(HttpHeaders.HOST, bucketName + "." + ep)
                                )
                                .requestFactory(requestFactoryBuilder.build(HttpClientSettings.defaults()
                                        .withReadTimeout(Duration.ofMillis(ossTimeout))))
                                .defaultStatusHandler(HttpStatusCode::isError, (_, response) -> {
                                    String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                                    Result<?> result = jsonMapper.readValue(responseBody, Result.class);
                                    throw new MissException(result.msg());
                                })
                );
            }


            // Auth 组配置 - 添加追踪和认证拦截器
            groups.filterByName("auth").forEachClient((_, builder) ->
                    builder
                            .baseUrl(authUrl)
                            .uriBuilderFactory(authUriBuilderFactory)
                            .observationRegistry(observationRegistry)
                            .requestInterceptors(interceptors ->
                                    interceptors.add(authHttpInterceptor)
                            )
                            .requestFactory(requestFactoryBuilder.build(HttpClientSettings.defaults()
                                    .withReadTimeout(Duration.ofMillis(authTimeout))))
                            .defaultStatusHandler(HttpStatusCode::isError, (_, response) -> {
                                String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                                Result<?> result = jsonMapper.readValue(responseBody, Result.class);
                                throw new MissException(result.msg());
                            })
            );

            // Search 组配置 - 添加追踪和认证拦截器
            groups.filterByName("search").forEachClient((_, builder) ->
                    builder
                            .baseUrl(searchUrl)
                            .uriBuilderFactory(searchUriBuilderFactory)
                            .observationRegistry(observationRegistry)
                            .requestInterceptors(interceptors ->
                                    interceptors.add(authHttpInterceptor)
                            )
                            .requestFactory(requestFactoryBuilder.build(HttpClientSettings.defaults()
                                    .withReadTimeout(Duration.ofMillis(searchTimeout))))
                            .defaultStatusHandler(HttpStatusCode::isError, (_, response) -> {
                                String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                                Result<?> result = jsonMapper.readValue(responseBody, Result.class);
                                throw new MissException(result.msg());
                            })
            );

            // Blog组配置 - 添加追踪和认证拦截器
            groups.filterByName("blog").forEachClient((_, builder) ->
                    builder
                            .baseUrl(blogUrl)
                            .uriBuilderFactory(blogUriBuilderFactory)
                            .observationRegistry(observationRegistry)
                            .requestInterceptors(interceptors ->
                                    interceptors.add(authHttpInterceptor)
                            )
                            .requestFactory(requestFactoryBuilder.build(HttpClientSettings.defaults()
                                    .withReadTimeout(Duration.ofMillis(blogTimeout))))
                            .defaultStatusHandler(HttpStatusCode::isError, (_, response) -> {
                                String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                                Result<?> result = jsonMapper.readValue(responseBody, Result.class);
                                throw new MissException(result.msg());
                            })
            );

            if (StringUtils.hasLength(smsUrl)) {
                //Sms 组配置 - 添加追踪和认证拦截器
                groups.filterByName("sms").forEachClient((_, builder) ->
                        builder
                                .baseUrl(smsUrl)
                                .uriBuilderFactory(smsUriBuilderFactory)
                                .observationRegistry(observationRegistry)
                                .requestFactory(requestFactoryBuilder.build(HttpClientSettings.defaults()
                                        .withReadTimeout(Duration.ofMillis(smsTimeout))))
                                .defaultStatusHandler(HttpStatusCode::isError, (_, response) -> {
                                    String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                                    Result<?> result = jsonMapper.readValue(responseBody, Result.class);
                                    throw new MissException(result.msg());
                                })
                );
            }
        };
    }
}
