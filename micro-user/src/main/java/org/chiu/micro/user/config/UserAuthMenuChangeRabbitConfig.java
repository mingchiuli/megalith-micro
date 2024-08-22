package org.chiu.micro.user.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author mingchiuli
 * @create 2022-12-25 4:13 pm
 */
@Configuration
public class UserAuthMenuChangeRabbitConfig {

    public static final String QUEUE = "user.auth.menu.change.queue.auth";

    public static final String FANOUT_EXCHANGE = "user.auth.menu.change.fanout.exchange";

    @Bean("authQueue")
    Queue authQueue() {
        return new Queue(QUEUE, true, false, false);
    }

    //ES交换机
    @Bean("fanoutExchange")
    FanoutExchange exchange() {
        return new FanoutExchange(FANOUT_EXCHANGE, true, false);
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
