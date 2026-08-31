package wiki.chiu.micro.common.rpc.config;

import io.micrometer.observation.ObservationRegistry;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.function.Consumer;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.HttpClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientHttpServiceGroupConfigurer;
import org.springframework.web.service.registry.HttpServiceGroupConfigurer.Groups;
import org.springframework.web.util.DefaultUriBuilderFactory;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.common.exception.RemoteServiceException;
import wiki.chiu.micro.common.lang.CommonErrorCode;
import wiki.chiu.micro.common.lang.ErrorCode;
import wiki.chiu.micro.common.lang.ErrorCodes;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.config.interceptor.AuthHttpInterceptor;

@AutoConfiguration
@EnableConfigurationProperties(HttpClientProperties.class)
public class HttpGroupConfig {

    @Bean
    AuthHttpInterceptor tokenInterceptor() {
        return new AuthHttpInterceptor();
    }

    @Bean
    RestClientHttpServiceGroupConfigurer groupConfigurer(
        AuthHttpInterceptor authInterceptor,
        JsonMapper jsonMapper,
        ClientHttpRequestFactoryBuilder<?> requestFactoryBuilder,
        ObservationRegistry observationRegistry,
        HttpClientProperties properties) {
        return groups -> {
            configure(
                groups,
                new ClientSpec("user", properties.userUrl(), properties.http().userTimeout(), true, null),
                authInterceptor,
                jsonMapper,
                requestFactoryBuilder,
                observationRegistry);
            configure(
                groups,
                new ClientSpec("auth", properties.authUrl(), properties.http().authTimeout(), true, null),
                authInterceptor,
                jsonMapper,
                requestFactoryBuilder,
                observationRegistry);
            configure(
                groups,
                new ClientSpec(
                    "search", properties.searchUrl(), properties.http().searchTimeout(), true, null),
                authInterceptor,
                jsonMapper,
                requestFactoryBuilder,
                observationRegistry);
            configure(
                groups,
                new ClientSpec("blog", properties.blogUrl(), properties.http().blogTimeout(), true, null),
                authInterceptor,
                jsonMapper,
                requestFactoryBuilder,
                observationRegistry);

            String ossUrl = properties.oss().baseUrl();
            if (StringUtils.hasLength(ossUrl)) {
                String host = properties.aliyun().oss().bucketName() + "." + properties.oss().endpoint();
                configure(
                    groups,
                    new ClientSpec(
                        "oss",
                        ossUrl,
                        properties.http().ossTimeout(),
                        false,
                        headers -> headers.add(HttpHeaders.HOST, host)),
                    authInterceptor,
                    jsonMapper,
                    requestFactoryBuilder,
                    observationRegistry);
            }

            String smsUrl = properties.sms().baseUrl();
            if (StringUtils.hasLength(smsUrl)) {
                configure(
                    groups,
                    new ClientSpec("sms", smsUrl, properties.http().smsTimeout(), false, null),
                    authInterceptor,
                    jsonMapper,
                    requestFactoryBuilder,
                    observationRegistry);
            }
        };
    }

    private static void configure(
        Groups<RestClient.Builder> groups,
        ClientSpec spec,
        AuthHttpInterceptor authInterceptor,
        JsonMapper jsonMapper,
        ClientHttpRequestFactoryBuilder<?> requestFactoryBuilder,
        ObservationRegistry observationRegistry) {
        DefaultUriBuilderFactory uriFactory = new DefaultUriBuilderFactory(spec.baseUrl());
        uriFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.TEMPLATE_AND_VALUES);

        groups
            .filterByName(spec.name())
            .forEachClient(
                (_, builder) -> {
                    builder
                        .baseUrl(spec.baseUrl())
                        .uriBuilderFactory(uriFactory)
                        .observationRegistry(observationRegistry)
                        .requestFactory(
                            requestFactoryBuilder.build(
                                HttpClientSettings.defaults()
                                    .withReadTimeout(Duration.ofMillis(spec.timeoutMillis()))))
                        .defaultStatusHandler(
                            HttpStatusCode::isError,
                            (_, response) -> handleRemoteError(response, jsonMapper));
                    if (spec.authenticated()) {
                        builder.requestInterceptors(interceptors -> interceptors.add(authInterceptor));
                    }
                    if (spec.defaultHeaders() != null) {
                        builder.defaultHeaders(spec.defaultHeaders());
                    }
                });
    }

    private static void handleRemoteError(ClientHttpResponse response, JsonMapper jsonMapper)
        throws IOException {
        int status = response.getStatusCode().value();
        String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
        Result<?> result = parseResult(responseBody, jsonMapper);
        ErrorCode errorCode =
            status >= 500
                ? CommonErrorCode.DOWNSTREAM_ERROR
                : ErrorCodes.find(result == null || result.code() == null ? status : result.code())
                  .orElse(CommonErrorCode.DOWNSTREAM_ERROR);
        String message =
            result == null || !StringUtils.hasText(result.msg())
                ? "downstream service returned HTTP " + status
                : result.msg();
        throw new RemoteServiceException(errorCode, message, status);
    }

    private static Result<?> parseResult(String responseBody, JsonMapper jsonMapper) {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }
        try {
            return jsonMapper.readValue(responseBody, Result.class);
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private record ClientSpec(
        String name,
        String baseUrl,
        int timeoutMillis,
        boolean authenticated,
        Consumer<HttpHeaders> defaultHeaders) {
    }
}
