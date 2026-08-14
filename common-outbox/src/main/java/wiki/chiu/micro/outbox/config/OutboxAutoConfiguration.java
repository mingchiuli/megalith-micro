package wiki.chiu.micro.outbox.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.outbox.OutboxEndpoint;
import wiki.chiu.micro.outbox.OutboxPublisher;
import wiki.chiu.micro.outbox.OutboxService;
import wiki.chiu.micro.outbox.OutboxStore;

@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(OutboxProperties.class)
@ConditionalOnProperty(
    prefix = "megalith.outbox",
    name = {"producer", "exchange"})
public class OutboxAutoConfiguration {

  @Bean
  OutboxStore outboxStore(JdbcTemplate jdbcTemplate) {
    return new OutboxStore(jdbcTemplate);
  }

  @Bean
  OutboxService outboxService(OutboxStore store, JsonMapper jsonMapper) {
    return new OutboxService(store, jsonMapper);
  }

  @Bean
  OutboxPublisher outboxPublisher(
      OutboxStore store,
      RabbitTemplate rabbitTemplate,
      RedissonClient redissonClient,
      OutboxProperties properties,
      io.micrometer.core.instrument.MeterRegistry meterRegistry) {
    return new OutboxPublisher(store, rabbitTemplate, redissonClient, properties, meterRegistry);
  }

  @Bean
  OutboxEndpoint outboxEndpoint(
      OutboxStore store, OutboxProperties properties, RedissonClient redissonClient) {
    return new OutboxEndpoint(store, properties, redissonClient);
  }

  @Bean(destroyMethod = "shutdown")
  @ConditionalOnMissingBean(RedissonClient.class)
  RedissonClient outboxRedissonClient(
      @Value("${spring.data.redis.host}") String host,
      @Value("${spring.data.redis.port}") int port,
      @Value("${spring.data.redis.password:}") String password) {
    Config config = new Config();
    config.setCodec(new StringCodec());
    config.setLockWatchdogTimeout(30_000);
    SingleServerConfig server = config.useSingleServer().setAddress("redis://" + host + ":" + port);
    if (StringUtils.hasText(password)) {
      server.setPassword(password);
    }
    return Redisson.create(config);
  }
}
