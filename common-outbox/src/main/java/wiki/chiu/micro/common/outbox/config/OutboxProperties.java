package wiki.chiu.micro.common.outbox.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;

import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

@ConfigurationProperties("megalith.outbox")
public class OutboxProperties {

    private Map<OutboxProducer, String> exchanges = new HashMap<>();
    private Map<String, String> eventExchanges = new HashMap<>();
    private int batchSize = 50;
    private int publisherConcurrency = 16;
    private long confirmTimeoutMillis = 5000;

    public Map<OutboxProducer, String> getExchanges() {
        return exchanges;
    }

    public void setExchanges(Map<OutboxProducer, String> exchanges) {
        this.exchanges = Map.copyOf(exchanges);
    }

    public String defaultExchange(OutboxProducer producer) {
        String exchange = exchanges.get(producer);
        if (exchange == null || exchange.isBlank()) {
            throw new IllegalArgumentException("Missing outbox exchange for " + producer);
        }
        return exchange;
    }

    public Map<String, String> getEventExchanges() {
        return eventExchanges;
    }

    public void setEventExchanges(Map<String, String> eventExchanges) {
        this.eventExchanges = eventExchanges == null ? new HashMap<>() : new HashMap<>(eventExchanges);
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int getPublisherConcurrency() {
        return publisherConcurrency;
    }

    public void setPublisherConcurrency(int publisherConcurrency) {
        this.publisherConcurrency = publisherConcurrency;
    }

    public long getConfirmTimeoutMillis() {
        return confirmTimeoutMillis;
    }

    public void setConfirmTimeoutMillis(long confirmTimeoutMillis) {
        this.confirmTimeoutMillis = confirmTimeoutMillis;
    }
}
