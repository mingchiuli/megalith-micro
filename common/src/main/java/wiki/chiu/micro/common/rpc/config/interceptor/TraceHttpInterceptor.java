package wiki.chiu.micro.common.rpc.config.interceptor;

import brave.Tracing;
import brave.http.HttpTracing;
import brave.spring.web.TracingClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestInterceptor;

public class TraceHttpInterceptor {

    public static ClientHttpRequestInterceptor tracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        return TracingClientHttpRequestInterceptor.create(httpTracing);
    }
}
