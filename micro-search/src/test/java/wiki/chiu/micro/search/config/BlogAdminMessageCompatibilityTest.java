package wiki.chiu.micro.search.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

import wiki.chiu.micro.common.message.BlogAdminIndexMessage;

class BlogAdminMessageCompatibilityTest {

    @Test
    void acceptsQueuedEventsWithoutTheNewPublishedRevisionSnapshot() {
        String payload = """
            {"eventId":"old-event","operation":"DRAFT_UPSERT",
             "blog":{"id":7,"userId":42,"publishedRevisionId":11,"adminRevision":3},
             "draft":null,"revision":null}
            """;
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setInferredArgumentType(BlogAdminIndexMessage.class);

        BlogAdminIndexMessage event =
            (BlogAdminIndexMessage)
                new JacksonJsonMessageConverter()
                    .fromMessage(
                        new Message(
                            payload.getBytes(StandardCharsets.UTF_8), properties));

        assertThat(event.blog().id()).isEqualTo(7L);
        assertThat(event.publishedRevision()).isNull();
    }
}
