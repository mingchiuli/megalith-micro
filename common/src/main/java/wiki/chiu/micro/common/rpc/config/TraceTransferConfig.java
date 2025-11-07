package wiki.chiu.micro.common.rpc.config;

import brave.Tracing;
import brave.spring.web.TracingClientHttpRequestInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiki.chiu.micro.common.rpc.config.interceptor.TraceHttpInterceptor;

@Configuration
public class TraceTransferConfig {

    @Bean
    public TracingClientHttpRequestInterceptor tracingInterceptor(ObjectProvider<Tracing> tracingProvider) {
        Tracing tracing = tracingProvider.getIfAvailable();
        if (tracing != null) {
            return (TracingClientHttpRequestInterceptor) TraceHttpInterceptor.tracingInterceptor(tracing);
        }
        // 没有 tracing 时不注册拦截器
        return null;
    }
}
