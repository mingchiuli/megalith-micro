package wiki.chiu.micro.blog.config;

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
public class BlogChangeRabbitConfig {

    public static final String ES_QUEUE = "blog.change.queue.es";

    public static final String CACHE_QUEUE = "blog.change.queue.cache";

    public static final String FANOUT_EXCHANGE = "blog.change.fanout.exchange";

    @Bean("esQueue")
    Queue esQueue() {
        return new Queue(ES_QUEUE, true, false, false);
    }

    @Bean("cacheQueue")
    Queue cahceQueue() {
        return new Queue(CACHE_QUEUE, true, false, false);
    }

    //ES交换机
    @Bean("fanoutExchange")
    FanoutExchange exchange() {
        return new FanoutExchange(FANOUT_EXCHANGE, true, false);
    }

    //绑定ES队列和ES交换机
    @Bean("esBinding")
    Binding esBinding(@Qualifier("esQueue") Queue esQueue,
                      @Qualifier("fanoutExchange") FanoutExchange exchange) {
        return BindingBuilder
                .bind(esQueue)
                .to(exchange);
    }

    @Bean("cacheBinding")
    Binding cacheBinding(@Qualifier("cacheQueue") Queue cacheQueue,
                         @Qualifier("fanoutExchange") FanoutExchange exchange) {
        return BindingBuilder
                .bind(cacheQueue)
                .to(exchange);
    }
}
