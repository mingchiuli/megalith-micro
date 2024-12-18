package wiki.chiu.micro.user.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wiki.chiu.micro.common.lang.Const;

/**
 * @author mingchiuli
 * @create 2022-12-25 4:13 pm
 */
@Configuration
public class UserAuthMenuChangeRabbitConfig {



    @Bean("authQueue")
    Queue authQueue() {
        return new Queue(Const.USER_QUEUE, true, false, false);
    }

    //ES交换机
    @Bean("fanoutExchange")
    FanoutExchange exchange() {
        return new FanoutExchange(Const.USER_CHANGE_FANOUT_EXCHANGE, true, false);
    }

    //绑定ES队列和ES交换机
    @Bean("authBinding")
    Binding esBinding(@Qualifier("authQueue") Queue authQueue,
                      @Qualifier("fanoutExchange") FanoutExchange exchange) {
        return BindingBuilder
                .bind(authQueue)
                .to(exchange);
    }
}
