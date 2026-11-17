package wiki.chiu.micro.exhibit;

import com.rabbitmq.client.ConnectionFactory;

import org.testcontainers.containers.GenericContainer;

import wiki.chiu.micro.common.constant.Const;

final class ExhibitNativeTestSupport {

    private ExhibitNativeTestSupport() {
    }

    static void declareBlogEvents(GenericContainer<?> rabbit) throws Exception {
        var factory = new ConnectionFactory();
        factory.setHost(rabbit.getHost());
        factory.setPort(rabbit.getMappedPort(5672));
        factory.setUsername("test");
        factory.setPassword("test-password");
        // micro-blog provisions this topology before exhibit starts in production.
        try (var connection = factory.newConnection(); var channel = connection.createChannel()) {
            channel.exchangeDeclare(Const.BLOG_CHANGE_FANOUT_EXCHANGE, "fanout", true);
            channel.queueDeclare(Const.EXHIBIT_STATE_QUEUE, true, false, false, null);
            channel.queueBind(Const.EXHIBIT_STATE_QUEUE, Const.BLOG_CHANGE_FANOUT_EXCHANGE, "");
        }
    }
}
