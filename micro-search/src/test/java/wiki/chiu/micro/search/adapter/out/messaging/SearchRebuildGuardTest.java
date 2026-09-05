package wiki.chiu.micro.search.adapter.out.messaging;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.ChannelCallback;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import wiki.chiu.micro.common.lang.Const;
import wiki.chiu.micro.messaging.RetryTopology;
import wiki.chiu.micro.scheduling.RedisTaskLock;

class SearchRebuildGuardTest {

    @Test
    void normalModeRejectsRebuildWithoutAcquiringLocks() {
        var lock = mock(RedisTaskLock.class);
        var rabbit = mock(RabbitTemplate.class);
        var guard = new SearchRebuildGuard(lock, rabbit, false);

        assertThatThrownBy(() -> guard.runExclusive(() -> "unexpected")).hasMessageContaining("maintenance mode");
        verifyNoInteractions(lock, rabbit);
    }

    @Test
    void retriesDeadLettersAndActiveConsumersEachBlockRebuild() throws Exception {
        var rabbit = mock(RabbitTemplate.class);
        var channel = mock(Channel.class);
        var empty = mock(AMQP.Queue.DeclareOk.class);
        when(rabbit.execute(any())).thenAnswer(call -> ((ChannelCallback<?>) call.getArgument(0)).doInRabbit(channel));
        var guard = new SearchRebuildGuard(mock(RedisTaskLock.class), rabbit, true);

        for (String queue : java.util.List.of(Const.ES_QUEUE + ".retry.3", RetryTopology.deadLetterQueue(Const.ES_QUEUE))) {
            when(channel.queueDeclarePassive(anyString())).thenReturn(empty);
            var pending = mock(AMQP.Queue.DeclareOk.class);
            when(pending.getMessageCount()).thenReturn(1);
            when(channel.queueDeclarePassive(queue)).thenReturn(pending);
            assertThatThrownBy(guard::requireQuiescent).hasMessageContaining(queue);
        }
        when(channel.queueDeclarePassive(anyString())).thenReturn(empty);
        var active = mock(AMQP.Queue.DeclareOk.class);
        when(active.getConsumerCount()).thenReturn(1);
        when(channel.queueDeclarePassive(Const.ES_QUEUE)).thenReturn(active);
        assertThatThrownBy(guard::requireQuiescent).hasMessageContaining(Const.ES_QUEUE);
    }
}
