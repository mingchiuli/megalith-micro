package wiki.chiu.micro.common.rpc.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AuthHttpInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request, byte @NonNull [] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
        try {
            HttpServletRequest req = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
            String token = Optional.ofNullable(req.getHeader(HttpHeaders.AUTHORIZATION)).orElse("");
            request.getHeaders().putIfAbsent(HttpHeaders.AUTHORIZATION, List.of(token));
        } catch (IllegalStateException _) {
        }

        return execution.execute(request, body);
    }


}