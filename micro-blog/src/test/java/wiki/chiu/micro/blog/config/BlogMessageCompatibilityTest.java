package wiki.chiu.micro.blog.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

import wiki.chiu.micro.common.lang.BlogChangedMessage;

class BlogMessageCompatibilityTest {

    @Test
    void theExistingConverterAcceptsQueuedEventsWithLegacyPaginationFields() {
        String payload = """
            {"eventId":"old-event","operation":0,"revision":11,"operatorUserId":42,
             "blogSnapshot":{"id":7,"revision":11,"title":"article"},
             "totalCount":100,"newerOrSameCount":10,"previousTotalCount":101}
            """;
        MessageProperties properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        properties.setInferredArgumentType(BlogChangedMessage.class);
        var converter = new JacksonJsonMessageConverter();

        BlogChangedMessage event = (BlogChangedMessage) converter.fromMessage(
            new Message(payload.getBytes(StandardCharsets.UTF_8), properties));

        assertThat(event.revision()).isEqualTo(11L);
        assertThat(event.blogSnapshot().id()).isEqualTo(7L);
        assertThat(new String(converter.toMessage(event, new MessageProperties()).getBody(), StandardCharsets.UTF_8))
            .doesNotContain("totalCount", "newerOrSameCount", "previousTotalCount");
    }
}
