package wiki.chiu.micro.common.rpc.config;

import brave.Tracing;
import brave.spring.web.TracingClientHttpRequestInterceptor;
import jakarta.annotation.Resource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiki.chiu.micro.common.rpc.config.interceptor.TraceHttpInterceptor;

@Configuration
@ConditionalOnBean(Tracing.class)
public class TraceTransferConfig {

    @Resource
    private Tracing tracing;

    @Bean
    TracingClientHttpRequestInterceptor tracingInterceptor() {
        return (TracingClientHttpRequestInterceptor) TraceHttpInterceptor.tracingInterceptor(tracing);
    }
}
