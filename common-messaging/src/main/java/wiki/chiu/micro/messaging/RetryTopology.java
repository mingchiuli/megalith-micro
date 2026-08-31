package wiki.chiu.micro.messaging;

import java.util.List;
import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;

public final class RetryTopology {

    private RetryTopology() {
    }

    public static Declarables forQueue(String mainQueue) {
        DirectExchange retryExchange = new DirectExchange(retryExchange(mainQueue), true, false);
        Queue first = retryQueue(mainQueue, 1, 5_000);
        Queue second = retryQueue(mainQueue, 2, 30_000);
        Queue third = retryQueue(mainQueue, 3, 300_000);
        Queue deadLetter =
            new Queue(
                deadLetterQueue(mainQueue),
                true,
                false,
                false,
                Map.of("x-message-ttl", 14 * 24 * 60 * 60 * 1000));
        Binding firstBinding = BindingBuilder.bind(first).to(retryExchange).with("retry.1");
        Binding secondBinding = BindingBuilder.bind(second).to(retryExchange).with("retry.2");
        Binding thirdBinding = BindingBuilder.bind(third).to(retryExchange).with("retry.3");
        return new Declarables(
            List.<Declarable>of(
                retryExchange,
                first,
                second,
                third,
                deadLetter,
                firstBinding,
                secondBinding,
                thirdBinding));
    }

    public static String retryExchange(String mainQueue) {
        return mainQueue + ".retry.exchange";
    }

    public static String deadLetterQueue(String mainQueue) {
        return mainQueue + ".dlq";
    }

    private static Queue retryQueue(String mainQueue, int attempt, int ttl) {
        return new Queue(
            mainQueue + ".retry." + attempt,
            true,
            false,
            false,
            Map.of(
                "x-message-ttl", ttl,
                "x-dead-letter-exchange", "",
                "x-dead-letter-routing-key", mainQueue));
    }
}
