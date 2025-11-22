package wiki.chiu.micro.common.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Result;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class RpcClientFactory {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T createHttpService(Class<T> serviceClass, String url, HttpClient httpClient, DefaultUriBuilderFactory.EncodingMode encodingMode, HttpHeaders headers, Duration timeout, List<? extends ClientHttpRequestInterceptor> clientHttpRequestInterceptors) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(timeout);

        DefaultUriBuilderFactory uriBuilderFactory;
        if (StringUtils.hasLength(url)) {
            uriBuilderFactory = new DefaultUriBuilderFactory(url);
        } else {
            uriBuilderFactory = new DefaultUriBuilderFactory();
        }

        if (encodingMode != null) {
            uriBuilderFactory.setEncodingMode(encodingMode);
        }

        RestClient.Builder clientBuilder = RestClient.builder()
                .baseUrl(url)
                .uriBuilderFactory(uriBuilderFactory)
                .requestFactory(requestFactory)
                .defaultStatusHandler(HttpStatusCode::isError, (_, response) -> {
                    String responseBody = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    // 尝试解析为 Result 对象
                    Result<?> result = objectMapper.readValue(responseBody, Result.class);
                    // 如果解析成功，直接抛出包含原始错误消息的异常
                    throw new MissException(result.msg());
                });

        if (!CollectionUtils.isEmpty(clientHttpRequestInterceptors)) {
            clientBuilder.requestInterceptors(interceptors -> interceptors.addAll(clientHttpRequestInterceptors));
        }

        if (!headers.isEmpty()) {
            clientBuilder.defaultHeaders(httpHeaders -> {
                for (Map.Entry<String, List<String>> entry : headers.headerSet()) {
                    httpHeaders.addAll(entry.getKey(), entry.getValue());
                }
            });
        }

        RestClient client = clientBuilder.build();
        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return factory.createClient(serviceClass);
    }
}
