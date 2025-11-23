package wiki.chiu.micro.common.rpc.config;

import brave.spring.web.TracingClientHttpRequestInterceptor;
import brave.Tracing;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import wiki.chiu.micro.common.rpc.config.interceptor.TraceHttpInterceptor;

@AutoConfiguration
public class TraceTransferConfig {

    @Bean
    public TracingClientHttpRequestInterceptor tracingInterceptor(ObjectProvider<@NonNull Tracing> tracingProvider) {
        Tracing tracing = tracingProvider.getIfAvailable();
        if (tracing != null) {
            return (TracingClientHttpRequestInterceptor) TraceHttpInterceptor.tracingInterceptor(tracing);
        }
        // 没有 tracing 时不注册拦截器
        throw new IllegalStateException("tracing not available");
    }
}