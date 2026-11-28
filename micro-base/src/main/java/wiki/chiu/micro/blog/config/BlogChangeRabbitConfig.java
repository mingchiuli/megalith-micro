package wiki.chiu.micro.blog.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import wiki.chiu.micro.common.constant.Const;

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

    @Bean("adminEsQueue")
    Queue adminEsQueue() {
        return new Queue(Const.ADMIN_ES_QUEUE, true, false, false);
    }

    @Bean("exhibitStateQueue")
    Queue exhibitStateQueue() {
        return new Queue(Const.EXHIBIT_STATE_QUEUE, true, false, false);
    }

    @Bean("recycleQueue")
    Queue recycleQueue() {
        return new Queue(Const.RECYCLE_QUEUE, true, false, false);
    }

    // ES交换机
    @Bean("fanoutExchange")
    FanoutExchange exchange() {
        return new FanoutExchange(Const.BLOG_CHANGE_FANOUT_EXCHANGE, true, false);
    }

    @Bean("adminFanoutExchange")
    FanoutExchange adminExchange() {
        return new FanoutExchange(Const.BLOG_ADMIN_FANOUT_EXCHANGE, true, false);
    }

    // 绑定ES队列和ES交换机
    @Bean("esBinding")
    Binding esBinding(
        @Qualifier("esQueue") Queue esQueue, @Qualifier("fanoutExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(esQueue).to(exchange);
    }

    @Bean("adminEsBinding")
    Binding adminEsBinding(
        @Qualifier("adminEsQueue") Queue queue,
        @Qualifier("adminFanoutExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange);
    }

    @Bean("exhibitStateBinding")
    Binding exhibitStateBinding(
        @Qualifier("exhibitStateQueue") Queue exhibitStateQueue,
        @Qualifier("fanoutExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(exhibitStateQueue).to(exchange);
    }

    @Bean("recycleBinding")
    Binding recycleBinding(
        @Qualifier("recycleQueue") Queue recycleQueue,
        @Qualifier("fanoutExchange") FanoutExchange exchange) {
        return BindingBuilder.bind(recycleQueue).to(exchange);
    }
}
