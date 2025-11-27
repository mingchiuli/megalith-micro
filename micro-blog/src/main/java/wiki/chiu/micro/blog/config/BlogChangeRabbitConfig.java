package wiki.chiu.micro.blog.config;

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
@Configuration(proxyBeanMethods = false)
public class BlogChangeRabbitConfig {


    @Bean("esQueue")
    Queue esQueue() {
        return new Queue(Const.ES_QUEUE, true, false, false);
    }

    @Bean("cacheQueue")
    Queue cacheQueue() {
        return new Queue(Const.CACHE_QUEUE, true, false, false);
    }

    //ES交换机
    @Bean("fanoutExchange")
    FanoutExchange exchange() {
        return new FanoutExchange(Const.BLOG_CHANGE_FANOUT_EXCHANGE, true, false);
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
