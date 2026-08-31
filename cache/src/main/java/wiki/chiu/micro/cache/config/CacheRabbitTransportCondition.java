package wiki.chiu.micro.cache.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

final class CacheRabbitTransportCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String transport =
            context
                .getEnvironment()
                .getProperty("megalith.cache.eviction.transport", "AUTO");
        if ("RABBIT".equalsIgnoreCase(transport)) {
            return true;
        }
        return "AUTO".equalsIgnoreCase(transport)
            && StringUtils.hasText(
            context
                .getEnvironment()
                .getProperty("megalith.cache.eviction.rabbit.queue-prefix"))
            && StringUtils.hasText(
            context.getEnvironment().getProperty("megalith.cache.eviction.rabbit.exchange"));
    }
}
