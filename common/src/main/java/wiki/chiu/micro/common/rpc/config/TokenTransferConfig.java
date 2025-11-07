package wiki.chiu.micro.common.rpc.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import wiki.chiu.micro.common.rpc.config.interceptor.AuthHttpInterceptor;

@AutoConfiguration
public class TokenTransferConfig {

    @Bean
    AuthHttpInterceptor tokenInterceptor() {
        return new AuthHttpInterceptor();
    }
}
