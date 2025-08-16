package wiki.chiu.micro.common.rpc.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import wiki.chiu.micro.common.rpc.AuthHttpService;
import wiki.chiu.micro.common.rpc.config.auth.AuthArgumentResolver;

import java.util.List;


@Configuration
@ConditionalOnBean(AuthHttpService.class)
public class AuthArgumentResolverConfig {

    private final AuthHttpService authHttpService;

    public AuthArgumentResolverConfig(AuthHttpService authHttpService) {
        this.authHttpService = authHttpService;
    }

    @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
                resolvers.add(new AuthArgumentResolver(authHttpService));
            }
        };
    }
}