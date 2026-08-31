package wiki.chiu.micro.user.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import wiki.chiu.micro.common.lang.UserDeletedMessage;
import wiki.chiu.micro.outbox.OutboxProducer;
import wiki.chiu.micro.outbox.OutboxService;

class UserDeletionOutboxTest {

    @Test
    void writesDedicatedDeletionEvent() {
        OutboxService outbox = Mockito.mock(OutboxService.class);
        UserDeletionOutbox deletions = new UserDeletionOutbox(outbox);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<String, Object>> factory = ArgumentCaptor.forClass(Function.class);

        deletions.enqueue(List.of(7L, 9L));

        verify(outbox)
            .enqueue(eq(OutboxProducer.USER), eq("USER_DELETION"), eq(7L), factory.capture());
        UserDeletedMessage event = (UserDeletedMessage) factory.getValue().apply("event-1");
        assertEquals("event-1", event.eventId());
        assertEquals(List.of(7L, 9L), event.userIds());
    }
}
