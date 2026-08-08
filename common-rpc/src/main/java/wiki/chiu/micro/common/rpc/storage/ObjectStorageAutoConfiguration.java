package wiki.chiu.micro.common.rpc.storage;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import wiki.chiu.micro.common.rpc.OssHttpService;
import wiki.chiu.micro.common.rpc.config.HttpClientProperties;

@AutoConfiguration(afterName = "wiki.chiu.micro.common.rpc.config.HttpGroupConfig")
@ConditionalOnBean(OssHttpService.class)
public class ObjectStorageAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  Clock objectStorageClock() {
    return Clock.systemUTC();
  }

  @Bean
  @ConditionalOnMissingBean
  ObjectStorageClient objectStorageClient(
      OssHttpService ossHttpService, HttpClientProperties properties, Clock objectStorageClock) {
    return new AliyunObjectStorageClient(ossHttpService, properties, objectStorageClock);
  }
}
