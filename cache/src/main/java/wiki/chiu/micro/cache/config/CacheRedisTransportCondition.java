package wiki.chiu.micro.cache.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

final class CacheRedisTransportCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String transport =
            context
                .getEnvironment()
                .getProperty("megalith.cache.eviction.transport", "AUTO");
        return "AUTO".equalsIgnoreCase(transport) || "REDIS".equalsIgnoreCase(transport);
    }
}
