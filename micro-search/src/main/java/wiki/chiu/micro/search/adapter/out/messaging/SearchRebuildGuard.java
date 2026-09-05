package wiki.chiu.micro.search.adapter.out.messaging;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.messaging.RetryTopology;
import wiki.chiu.micro.scheduling.RedisTaskLock;
import wiki.chiu.micro.search.application.model.IndexRebuildRejectedException;
import wiki.chiu.micro.search.application.port.out.SearchRebuildControl;

@Component
public class SearchRebuildGuard implements SearchRebuildControl {

    private final RedisTaskLock lock;
    private final RabbitTemplate rabbit;
    private final boolean maintenance;

    public SearchRebuildGuard(
        RedisTaskLock lock, RabbitTemplate rabbit,
        @Value("${megalith.search.maintenance.enabled:false}") boolean maintenance) {
        this.lock = lock;
        this.rabbit = rabbit;
        this.maintenance = maintenance;
    }

    @Override
    public <T> T runExclusive(Supplier<T> task) {
        requireMaintenance();
        return lock.run("search:index-rebuild", () -> lock.run("blog:search-read-count-sync", task));
    }

    @Override
    public void requireQuiescent() {
        requireMaintenance();
        List<String> queues = List.of(Const.ES_QUEUE, Const.ES_QUEUE + ".retry.1",
            Const.ES_QUEUE + ".retry.2", Const.ES_QUEUE + ".retry.3", RetryTopology.deadLetterQueue(Const.ES_QUEUE));
        rabbit.execute(channel -> {
            for (String queue : queues) {
                var state = channel.queueDeclarePassive(queue);
                if (state.getMessageCount() != 0 || state.getConsumerCount() != 0) {
                    throw new IndexRebuildRejectedException("search queue must be empty and stopped: " + queue);
                }
            }
            return null;
        });
    }

    private void requireMaintenance() {
        if (!maintenance) {
            throw new IndexRebuildRejectedException("search index rebuild requires maintenance mode");
        }
    }
}
