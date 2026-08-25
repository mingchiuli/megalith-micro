package wiki.chiu.micro.cache.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.cache.message.CacheEvictionMessage;

class CacheEvictRabbitConfigTest {

  private final CacheEvictRabbitConfig config = new CacheEvictRabbitConfig();

  @Test
  void converterTrustsOnlyTheCacheMessagePackage() {
    JacksonJsonMessageConverter converter =
        config.cacheEvictMessageConverter(JsonMapper.builder().build());
    CacheEvictionMessage expected = CacheEvictionMessage.of(java.util.Set.of("cache:v1:key"));

    Message encoded = converter.toMessage(expected, new MessageProperties());

    assertThat(converter.fromMessage(encoded)).isEqualTo(expected);
  }
}
