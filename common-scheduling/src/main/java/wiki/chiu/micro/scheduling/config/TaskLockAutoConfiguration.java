package wiki.chiu.micro.scheduling.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.scheduling.RedisTaskLock;
import wiki.chiu.micro.scheduling.TaskLockProperties;

@AutoConfiguration
@EnableScheduling
@EnableConfigurationProperties(TaskLockProperties.class)
public class TaskLockAutoConfiguration {

  @Bean
  RedisTaskLock redisTaskLock(RedissonClient redissonClient, TaskLockProperties properties) {
    return new RedisTaskLock(redissonClient, properties);
  }

  @Bean(destroyMethod = "shutdown")
  @ConditionalOnMissingBean(RedissonClient.class)
  RedissonClient taskLockRedissonClient(
      @Value("${spring.data.redis.host}") String host,
      @Value("${spring.data.redis.port}") int port,
      @Value("${spring.data.redis.password:}") String password) {
    Config config = new Config();
    config.setCodec(new StringCodec());
    config.setLockWatchdogTimeout(30_000);
    config.useSingleServer().setAddress("redis://" + host + ":" + port);
    if (StringUtils.hasText(password)) {
      config.setPassword(password);
    }
    return Redisson.create(config);
  }
}
