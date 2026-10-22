package wiki.chiu.micro.common.outbox.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.common.outbox.adapter.out.serialization.JacksonOutboxEventSerializer;
import wiki.chiu.micro.common.outbox.application.port.out.OutboxStore;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

class OutboxServiceImplTest {

    private final OutboxStore store = mock(OutboxStore.class);
    private final OutboxServiceImpl service =
        new OutboxServiceImpl(
            store, new JacksonOutboxEventSerializer(JsonMapper.builder().build()));

    @Test
    void eventFactoryReceivesThePersistedEventId() {
        String eventId =
            service.enqueue(OutboxProducer.BLOG, "BLOG", 42L, id -> new TestEvent(id, "changed"));

        ArgumentCaptor<String> payload = ArgumentCaptor.forClass(String.class);
        verify(store)
            .insert(
                eq(eventId),
                eq(OutboxProducer.BLOG),
                eq("BLOG"),
                eq("42"),
                eq("TestEvent"),
                payload.capture());
        assertTrue(payload.getValue().contains("\"eventId\":\"" + eventId + "\""));
        assertTrue(payload.getValue().contains("\"value\":\"changed\""));
    }

    @Test
    void directEventUsesItsRuntimeTypeAsEventType() {
        String eventId =
            service.enqueue(OutboxProducer.USER, "AUTHORIZATION", "GLOBAL", new TestEvent("1", "x"));

        ArgumentCaptor<String> storedId = ArgumentCaptor.forClass(String.class);
        verify(store)
            .insert(
                storedId.capture(),
                eq(OutboxProducer.USER),
                eq("AUTHORIZATION"),
                eq("GLOBAL"),
                eq("TestEvent"),
                anyString());
        assertEquals(eventId, storedId.getValue());
    }

    private record TestEvent(String eventId, String value) {
    }
}
