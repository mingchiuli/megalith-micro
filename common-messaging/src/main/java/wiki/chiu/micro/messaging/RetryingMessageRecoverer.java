package wiki.chiu.micro.messaging;

import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

public class RetryingMessageRecoverer {

    private static final Logger log = LoggerFactory.getLogger(RetryingMessageRecoverer.class);
    private static final String RETRY_HEADER = "x-megalith-retry-count";

    private final RabbitTemplate rabbitTemplate;
    private final String mainQueue;

    public RetryingMessageRecoverer(RabbitTemplate rabbitTemplate, String mainQueue) {
        this.rabbitTemplate = rabbitTemplate;
        this.mainQueue = mainQueue;
    }

    public void recover(Message original, Channel channel, Exception failure) {
        long deliveryTag = original.getMessageProperties().getDeliveryTag();
        int previousAttempts = retryCount(original);
        int nextAttempt = previousAttempts + 1;
        String correlationId =
            Optional.ofNullable(original.getMessageProperties().getMessageId())
                .orElseGet(() -> Long.toString(deliveryTag));
        Message retry =
            MessageBuilder.fromClonedMessage(original)
                .setHeader(RETRY_HEADER, nextAttempt)
                .setMessageId(correlationId)
                .build();
        String exchange = nextAttempt <= 3 ? RetryTopology.retryExchange(mainQueue) : "";
        String routingKey =
            nextAttempt <= 3 ? "retry." + nextAttempt : RetryTopology.deadLetterQueue(mainQueue);

        try {
            CorrelationData correlation = new CorrelationData(correlationId + ":retry:" + nextAttempt);
            rabbitTemplate.send(exchange, routingKey, retry, correlation);
            CorrelationData.Confirm confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirm.ack() || correlation.getReturned() != null) {
                throw new IllegalStateException("retry message was not accepted by RabbitMQ");
            }
            channel.basicAck(deliveryTag, false);
            if (nextAttempt > 3) {
                log.error("Message {} moved to {}", correlationId, routingKey, failure);
            } else {
                log.warn("Message {} scheduled for retry {}", correlationId, nextAttempt, failure);
            }
        } catch (InterruptedException retryFailure) {
            Thread.currentThread().interrupt();
            nackForRedelivery(channel, deliveryTag, correlationId, retryFailure);
        } catch (Exception retryFailure) {
            nackForRedelivery(channel, deliveryTag, correlationId, retryFailure);
        }
    }

    private void nackForRedelivery(
        Channel channel, long deliveryTag, String correlationId, Exception retryFailure) {
        log.error("Failed to schedule retry for message {}", correlationId, retryFailure);
        try {
            channel.basicNack(deliveryTag, false, true);
        } catch (IOException nackFailure) {
            log.error("Failed to nack message {}", correlationId, nackFailure);
        }
    }

    private int retryCount(Message message) {
        Object value = message.getMessageProperties().getHeaders().get(RETRY_HEADER);
        return value instanceof Number number ? number.intValue() : 0;
    }
}
