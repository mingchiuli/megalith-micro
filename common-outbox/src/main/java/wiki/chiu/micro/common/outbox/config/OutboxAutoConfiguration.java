package wiki.chiu.micro.common.outbox.config;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.common.outbox.adapter.in.actuator.OutboxEndpoint;
import wiki.chiu.micro.common.outbox.adapter.out.messaging.OutboxEventPublisher;
import wiki.chiu.micro.common.outbox.adapter.out.persistence.OutboxStoreAdapter;
import wiki.chiu.micro.common.outbox.adapter.out.persistence.repository.OutboxEventRepository;
import wiki.chiu.micro.common.outbox.adapter.out.serialization.JacksonOutboxEventSerializer;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxAdministration;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxEvents;
import wiki.chiu.micro.common.outbox.application.port.out.OutboxEventSerializer;
import wiki.chiu.micro.common.outbox.application.port.out.OutboxStore;
import wiki.chiu.micro.common.outbox.application.service.OutboxAdministrationServiceImpl;
import wiki.chiu.micro.common.outbox.application.service.OutboxServiceImpl;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;
import wiki.chiu.micro.common.scheduling.RedisTaskLock;

@AutoConfiguration
@EnableConfigurationProperties(OutboxProperties.class)
@ConditionalOnProperty(prefix = "megalith.outbox", name = "enabled", havingValue = "true")
public class OutboxAutoConfiguration {

    @Bean
    OutboxStore outboxStore(OutboxEventRepository repository) {
        return new OutboxStoreAdapter(repository);
    }

    @Bean
    OutboxEventSerializer outboxEventSerializer(JsonMapper jsonMapper) {
        return new JacksonOutboxEventSerializer(jsonMapper);
    }

    @Bean
    OutboxEvents outboxEvents(OutboxStore store, OutboxEventSerializer serializer) {
        return new OutboxServiceImpl(store, serializer);
    }

    @Bean
    OutboxAdministration outboxAdministration(OutboxStore store) {
        return new OutboxAdministrationServiceImpl(store);
    }

    @Bean
    OutboxEventPublisher userOutboxPublisher(
        OutboxStore store,
        RabbitTemplate rabbitTemplate,
        RedisTaskLock taskLock,
        OutboxProperties properties,
        MeterRegistry meterRegistry) {
        return publisher(OutboxProducer.USER, store, rabbitTemplate, taskLock, properties, meterRegistry);
    }

    @Bean
    OutboxEventPublisher blogOutboxPublisher(
        OutboxStore store,
        RabbitTemplate rabbitTemplate,
        RedisTaskLock taskLock,
        OutboxProperties properties,
        MeterRegistry meterRegistry) {
        return publisher(OutboxProducer.BLOG, store, rabbitTemplate, taskLock, properties, meterRegistry);
    }

    private OutboxEventPublisher publisher(
        OutboxProducer producer,
        OutboxStore store,
        RabbitTemplate rabbitTemplate,
        RedisTaskLock taskLock,
        OutboxProperties properties,
        MeterRegistry meterRegistry) {
        return new OutboxEventPublisher(
            store,
            rabbitTemplate,
            taskLock,
            producer,
            properties.getBatchSize(),
            properties.getPublisherConcurrency(),
            properties.getConfirmTimeoutMillis(),
            properties.defaultExchange(producer),
            properties.getEventExchanges(),
            meterRegistry);
    }

    @Bean
    OutboxEndpoint outboxEndpoint(
        OutboxAdministration administration, RedisTaskLock taskLock) {
        return new OutboxEndpoint(administration, taskLock);
    }
}
