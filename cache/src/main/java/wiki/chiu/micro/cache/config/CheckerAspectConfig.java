package wiki.chiu.micro.cache.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import wiki.chiu.micro.cache.aspect.CheckerAspect;
import wiki.chiu.micro.cache.handler.CheckerHandler;

import java.util.List;

@AutoConfiguration
@ConditionalOnBean(CheckerHandler.class)
public class CheckerAspectConfig {

    private final List<CheckerHandler> checkerHandlers;

    public CheckerAspectConfig(List<CheckerHandler> checkerHandlers) {
        this.checkerHandlers = checkerHandlers;
    }

    @Bean
    CheckerAspect checkerAspect() {
        return new CheckerAspect(checkerHandlers);
    }
}
