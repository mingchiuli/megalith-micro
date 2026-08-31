package wiki.chiu.micro.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import tools.jackson.databind.json.JsonMapper;

class OutboxServiceTest {

    @Test
    void eventFactoryReceivesThePersistedEventId() {
        OutboxStore store = mock(OutboxStore.class);
        OutboxService service = new OutboxService(store, JsonMapper.builder().build());

        String eventId =
            service.enqueue(OutboxProducer.BLOG, "BLOG", 42L, id -> new TestEvent(id, "changed"));

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(store)
            .insert(
                org.mockito.ArgumentMatchers.eq(eventId),
                org.mockito.ArgumentMatchers.eq(OutboxProducer.BLOG),
                org.mockito.ArgumentMatchers.eq("BLOG"),
                org.mockito.ArgumentMatchers.eq("42"),
                org.mockito.ArgumentMatchers.eq("TestEvent"),
                payload.capture());
        assertTrue(payload.getValue().contains("\"eventId\":\"" + eventId + "\""));
        assertTrue(payload.getValue().contains("\"value\":\"changed\""));
    }

    @Test
    void directEventUsesItsRuntimeTypeAsEventType() {
        OutboxStore store = mock(OutboxStore.class);
        OutboxService service = new OutboxService(store, JsonMapper.builder().build());

        String eventId =
            service.enqueue(OutboxProducer.USER, "AUTHORIZATION", "GLOBAL", new TestEvent("1", "x"));

        ArgumentCaptor<String> storedId = ArgumentCaptor.forClass(String.class);
        verify(store)
            .insert(
                storedId.capture(),
                org.mockito.ArgumentMatchers.eq(OutboxProducer.USER),
                org.mockito.ArgumentMatchers.eq("AUTHORIZATION"),
                org.mockito.ArgumentMatchers.eq("GLOBAL"),
                org.mockito.ArgumentMatchers.eq("TestEvent"),
                org.mockito.ArgumentMatchers.anyString());
        assertEquals(eventId, storedId.getValue());
    }

    private record TestEvent(String eventId, String value) {
    }
}
