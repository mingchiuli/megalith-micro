package wiki.chiu.micro.exhibit.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import wiki.chiu.micro.cache.handler.CacheEvictor;
import wiki.chiu.micro.cache.key.CacheKeyFactory;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.message.BlogChangedMessage;
import wiki.chiu.micro.common.messaging.RetryingMessageRecoverer;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.exhibit.application.model.BlogChange;
import wiki.chiu.micro.exhibit.application.port.in.BlogChangeMaintenance;

class BlogChangeMessageListenerTest {

    private final BlogChangeMaintenance maintenance = mock(BlogChangeMaintenance.class);
    private final RetryingMessageRecoverer recoverer = mock(RetryingMessageRecoverer.class);
    private final CacheKeyFactory keys = mock(CacheKeyFactory.class);
    private final CacheEvictor evictor = mock(CacheEvictor.class);
    private final BlogChangeMessageListener listener =
        new BlogChangeMessageListener(maintenance, recoverer, keys, evictor);
    private final Channel channel = mock(Channel.class);
    private final Message message = MessageBuilder.withBody(new byte[0]).setDeliveryTag(11L).build();

    @Test
    void translatesWireFieldsAndAcknowledgesAfterTheUseCase() throws Exception {
        listener.handler(event(BlogOperateEnum.UPDATE.getCode()), channel, message);
        var change = ArgumentCaptor.forClass(BlogChange.class);
        var ordered = inOrder(maintenance, channel);
        ordered.verify(maintenance).apply(change.capture());
        ordered.verify(channel).basicAck(11L, false);
        assertThat(change.getValue()).isEqualTo(new BlogChange(7L, BlogOperateEnum.UPDATE, 3L, 2));
    }

    @Test
    void failedMaintenanceIsRecoveredInsteadOfAcknowledged() throws Exception {
        var failure = new IllegalStateException("maintenance unavailable");
        doThrow(failure).when(maintenance).apply(any());
        listener.handler(event(BlogOperateEnum.REMOVE.getCode()), channel, message);
        verify(recoverer).recover(message, channel, failure);
        verify(channel, never()).basicAck(11L, false);
    }

    @Test
    void unknownWireOperationsAreRecoveredBeforeCallingTheUseCase() throws Exception {
        listener.handler(event(99), channel, message);
        verify(recoverer).recover(eq(message), eq(channel), any(MissException.class));
        verifyNoInteractions(maintenance);
        verify(channel, never()).basicAck(11L, false);
    }

    @Test
    void failedEvictionAfterSuccessfulMaintenanceRetriesBothVersionKeys() throws Exception {
        var event = publicationChange(3L, 31L, 30L);
        var affected = Set.of("old", "new");
        when(keys.generate(any(), eq(7L), eq(30L))).thenReturn("old");
        when(keys.generate(any(), eq(7L), eq(31L))).thenReturn("new");
        var failure = new IllegalStateException("broadcast failed");
        doThrow(failure).doNothing().when(evictor).evict(affected);
        listener.handler(event, channel, message);
        verify(channel, never()).basicAck(11L, false);
        verify(recoverer).recover(message, channel, failure);

        listener.handler(event, channel, message);
        verify(evictor, times(2)).evict(affected);
        verify(channel).basicAck(11L, false);
    }

    @Test
    void latePublicationStillEvictsItsPreviousVersion() throws Exception {
        when(keys.generate(any(), eq(7L), eq(30L))).thenReturn("v30");
        when(keys.generate(any(), eq(7L), eq(31L))).thenReturn("v31");
        when(keys.generate(any(), eq(7L), eq(32L))).thenReturn("v32");

        listener.handler(publicationChange(4L, 32L, 31L), channel, message);
        listener.handler(publicationChange(3L, 31L, 30L), channel, message);

        var order = inOrder(evictor, channel);
        order.verify(evictor).evict(Set.of("v31", "v32"));
        order.verify(channel).basicAck(11L, false);
        order.verify(evictor).evict(Set.of("v30", "v31"));
        order.verify(channel).basicAck(11L, false);
    }

    private BlogChangedMessage publicationChange(long sequence, long revisionId, long previousRevisionId) {
        var blog = new BlogSnapshot(7L, 42L, "title", "description", "content", null, null, 2,
            "", 1L, sequence, revisionId, null, null, 0, 2, List.of());
        return new BlogChangedMessage("event-" + sequence, BlogOperateEnum.UPDATE.getCode(), sequence,
            42L, blog, sequence, previousRevisionId);
    }

    private BlogChangedMessage event(int operation) {
        var now = LocalDateTime.now();
        return new BlogChangedMessage("event", operation, 3L, 42L,
            new BlogSnapshot(7L, 42L, "title", "description", "content", now, now, 2, "cover", 1L, 3L));
    }
}
