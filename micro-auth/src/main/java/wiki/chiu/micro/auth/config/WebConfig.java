package wiki.chiu.micro.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import wiki.chiu.micro.auth.resolver.AuthArgumentResolver;
import wiki.chiu.micro.auth.service.AuthService;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    private final AuthService authService;

    public WebConfig(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new AuthArgumentResolver(authService));
    }

}