package wiki.chiu.micro.blog.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;

import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.common.message.BlogChangedMessage;

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
        assertThat(event.adminRevision()).isNull();
        assertThat(event.blogSnapshot().id()).isEqualTo(7L);
        assertThat(new String(converter.toMessage(event, new MessageProperties()).getBody(), StandardCharsets.UTF_8))
            .doesNotContain("totalCount", "newerOrSameCount", "previousTotalCount");
    }

    @Test
    void legacyRecycleBinEntriesRemainReadableWithoutAnAdminRevision() {
        String stored = """
            {"id":7,"userId":42,"title":"article","description":"description",
             "content":"content","status":0,"link":"","readCount":2,
             "eventRevision":3,"sensitiveContent":[]}
            """;

        Blog blog = JsonMapper.builder().build().readValue(stored, Blog.class);

        assertThat(blog.id()).isEqualTo(7L);
        assertThat(blog.eventRevision()).isEqualTo(3L);
        assertThat(blog.adminRevision()).isNull();
    }
}
