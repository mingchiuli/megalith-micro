package wiki.chiu.micro.cache.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import wiki.chiu.micro.cache.aspect.CheckerAspect;
import wiki.chiu.micro.cache.handler.CheckerHandler;

@AutoConfiguration
public class CheckerAspectConfig {

    @Bean
    public CheckerAspect checkerAspect(ObjectProvider<CheckerHandler> handlersProvider) {
        var checkerHandlers = handlersProvider.stream().toList();
        if (checkerHandlers.isEmpty()) {
            // 不注册 Bean，Spring 容器里不会有 checkerAspect
            return null;
        }
        return new CheckerAspect(checkerHandlers);
    }
}