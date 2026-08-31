package wiki.chiu.micro.outbox.config;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.outbox.OutboxEndpoint;
import wiki.chiu.micro.outbox.OutboxEventRepository;
import wiki.chiu.micro.outbox.OutboxPublisher;
import wiki.chiu.micro.outbox.OutboxService;
import wiki.chiu.micro.outbox.OutboxStore;
import wiki.chiu.micro.scheduling.RedisTaskLock;

@AutoConfiguration
@EnableConfigurationProperties(OutboxProperties.class)
@ConditionalOnProperty(
    prefix = "megalith.outbox",
    name = {"producer", "exchange"})
public class OutboxAutoConfiguration {

    @Bean
    OutboxStore outboxStore(OutboxEventRepository repository) {
        return new OutboxStore(repository);
    }

    @Bean
    OutboxService outboxService(OutboxStore store, JsonMapper jsonMapper) {
        return new OutboxService(store, jsonMapper);
    }

    @Bean
    OutboxPublisher outboxPublisher(
        OutboxStore store,
        RabbitTemplate rabbitTemplate,
        RedisTaskLock taskLock,
        OutboxProperties properties,
        io.micrometer.core.instrument.MeterRegistry meterRegistry) {
        return new OutboxPublisher(store, rabbitTemplate, taskLock, properties, meterRegistry);
    }

    @Bean
    OutboxEndpoint outboxEndpoint(
        OutboxStore store, OutboxProperties properties, RedisTaskLock taskLock) {
        return new OutboxEndpoint(store, properties, taskLock);
    }
}
