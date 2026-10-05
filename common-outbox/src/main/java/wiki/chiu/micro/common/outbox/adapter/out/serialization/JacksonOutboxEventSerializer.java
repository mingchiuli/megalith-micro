package wiki.chiu.micro.common.outbox.adapter.out.serialization;

import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.common.outbox.application.port.out.OutboxEventSerializer;

public class JacksonOutboxEventSerializer implements OutboxEventSerializer {

    private final JsonMapper jsonMapper;

    public JacksonOutboxEventSerializer(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public String serialize(Object event) {
        return jsonMapper.writeValueAsString(event);
    }
}
