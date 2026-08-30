package wiki.chiu.micro.user.config;

import org.springframework.amqp.core.FanoutExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiki.chiu.micro.common.lang.Const;

@Configuration(proxyBeanMethods = false)
public class UserDeletionRabbitConfig {

  @Bean("userDeletedExchange")
  FanoutExchange userDeletedExchange() {
    return new FanoutExchange(Const.USER_DELETED_FANOUT_EXCHANGE, true, false);
  }
}
