package org.chiu.micro.exhibit.config;

import java.io.IOException;
import java.util.Optional;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class HttpInterceptor implements ClientHttpRequestInterceptor {

    @Override
	public @NonNull ClientHttpResponse intercept(@NonNull HttpRequest request, @NonNull byte[] body, @NonNull ClientHttpRequestExecution execution) throws IOException {
	    try {
            HttpServletRequest req = ((ServletRequestAttributes) (RequestContextHolder.currentRequestAttributes())).getRequest();
            String token = Optional.ofNullable(req.getHeader(HttpHeaders.AUTHORIZATION)).orElse("");
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, token);
	    } catch(IllegalStateException e) {}
        return execution.execute(request, body);
	}
    
    
}