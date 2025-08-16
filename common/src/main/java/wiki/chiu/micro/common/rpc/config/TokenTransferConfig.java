package wiki.chiu.micro.common.rpc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiki.chiu.micro.common.rpc.config.interceptor.AuthHttpInterceptor;

@Configuration
public class TokenTransferConfig {

    @Bean
    AuthHttpInterceptor tokenInterceptor() {
        return new AuthHttpInterceptor();
    }
}
