package wiki.chiu.micro.outbox.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import wiki.chiu.micro.outbox.OutboxProducer;

@ConfigurationProperties("megalith.outbox")
public class OutboxProperties {

  private OutboxProducer producer;
  private String exchange;
  private int batchSize = 50;
  private int publisherConcurrency = 16;
  private long confirmTimeoutMillis = 5000;

  public OutboxProducer getProducer() {
    return producer;
  }

  public void setProducer(OutboxProducer producer) {
    this.producer = producer;
  }

  public String getExchange() {
    return exchange;
  }

  public void setExchange(String exchange) {
    this.exchange = exchange;
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
