package wiki.chiu.micro.cache.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.mock.env.MockEnvironment;

class CacheTransportConditionTest {

    private final AnnotatedTypeMetadata metadata = mock(AnnotatedTypeMetadata.class);

    @Test
    void autoMakesConfiguredRabbitEligibleAndKeepsRedisAsFallback() {
        ConditionContext context = context("AUTO", true);

        assertThat(new CacheRabbitTransportCondition().matches(context, metadata)).isTrue();
        assertThat(new CacheRedisTransportCondition().matches(context, metadata)).isTrue();
    }

    @Test
    void autoUsesRedisWhenRabbitSettingsAreAbsent() {
        ConditionContext context = context("AUTO", false);

        assertThat(new CacheRabbitTransportCondition().matches(context, metadata)).isFalse();
        assertThat(new CacheRedisTransportCondition().matches(context, metadata)).isTrue();
    }

    @Test
    void explicitTransportSelectsOnlyThatTransport() {
        assertThat(new CacheRabbitTransportCondition().matches(context("RABBIT", false), metadata))
            .isTrue();
        assertThat(new CacheRedisTransportCondition().matches(context("RABBIT", false), metadata))
            .isFalse();
        assertThat(new CacheRabbitTransportCondition().matches(context("REDIS", true), metadata))
            .isFalse();
        assertThat(new CacheRedisTransportCondition().matches(context("REDIS", true), metadata))
            .isTrue();
    }

    @Test
    void unknownTransportSelectsNoInfrastructure() {
        ConditionContext context = context("UNKNOWN", true);

        assertThat(new CacheRabbitTransportCondition().matches(context, metadata)).isFalse();
        assertThat(new CacheRedisTransportCondition().matches(context, metadata)).isFalse();
    }

    private ConditionContext context(String transport, boolean rabbitConfigured) {
        ConditionContext context = mock(ConditionContext.class);
        MockEnvironment environment =
            new MockEnvironment().withProperty("megalith.cache.eviction.transport", transport);
        if (rabbitConfigured) {
            environment
                .withProperty("megalith.cache.eviction.rabbit.queue-prefix", "cache.queue.")
                .withProperty("megalith.cache.eviction.rabbit.exchange", "cache.exchange");
        }
        when(context.getEnvironment()).thenReturn(environment);
        return context;
    }
}
