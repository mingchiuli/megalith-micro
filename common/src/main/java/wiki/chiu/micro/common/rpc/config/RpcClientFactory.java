package wiki.chiu.micro.common.rpc.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class RpcClientFactory {

    public static <T> T createHttpService(Class<T> serviceClass, String url, HttpClient httpClient, AuthHttpInterceptor interceptor, DefaultUriBuilderFactory.EncodingMode encodingMode, HttpHeaders headers) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

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
                .requestFactory(requestFactory);

        if (!CollectionUtils.isEmpty(headers)) {
            clientBuilder.defaultHeaders(httpHeaders -> {
                for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    httpHeaders.addAll(entry.getKey(), entry.getValue());
                }
            });
        }

        if (interceptor != null) {
            clientBuilder.requestInterceptor(interceptor);
        }

        RestClient client = clientBuilder.build();
        RestClientAdapter restClientAdapter = RestClientAdapter.create(client);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(restClientAdapter).build();
        return factory.createClient(serviceClass);
    }
}
