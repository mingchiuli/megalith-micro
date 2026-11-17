package wiki.chiu.micro.exhibit;

import static org.assertj.core.api.Assertions.assertThat;

import com.rabbitmq.client.ConnectionFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.common.constant.Const;

@EnabledIfEnvironmentVariable(named = "MICRO_EXHIBIT_NATIVE_IMAGE", matches = ".+")
class ExhibitNativeImageIntegrationTest {

    @Test
    void startsPublishedNativeImageWithRedisHostname() throws Exception {
        try (Network network = Network.newNetwork();
            GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:8-alpine"));
            GenericContainer<?> rabbit = new GenericContainer<>(DockerImageName.parse("rabbitmq:4.3.4-management"));
            GenericContainer<?> application = new GenericContainer<>(
                DockerImageName.parse(System.getenv("MICRO_EXHIBIT_NATIVE_IMAGE")))) {
            redis.withNetwork(network).withNetworkAliases("redis")
                .withCommand("redis-server", "--requirepass", "test-password");
            rabbit.withNetwork(network).withNetworkAliases("rabbitmq").withExposedPorts(5672)
                .withEnv("RABBITMQ_DEFAULT_USER", "test")
                .withEnv("RABBITMQ_DEFAULT_PASS", "test-password")
                .waitingFor(Wait.forLogMessage(".*Server startup complete.*\\n", 1));
            redis.start();
            rabbit.start();
            declareBlogEvents(rabbit);

            application.withNetwork(network).withExposedPorts(8083)
                .withEnv(Map.of(
                    "SPRING_DATA_REDIS_HOST", "redis",
                    "SPRING_DATA_REDIS_PORT", "6379",
                    "SPRING_DATA_REDIS_PASSWORD", "test-password",
                    "SPRING_RABBITMQ_HOST", "rabbitmq",
                    "SPRING_RABBITMQ_PORT", "5672",
                    "SPRING_RABBITMQ_USERNAME", "test",
                    "SPRING_RABBITMQ_PASSWORD", "test-password",
                    "MEGALITH_BLOG_EXISTENCE_INDEX_INITIAL_DELAY", "1h",
                    "MANAGEMENT_OTLP_METRICS_EXPORT_ENABLED", "false"))
                .waitingFor(Wait.forHttp("/actuator/health").forStatusCode(200));
            application.start();
            var response = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create("http://" + application.getHost() + ":"
                    + application.getMappedPort(8083) + "/public/blog/stat"))
                    .timeout(Duration.ofSeconds(5)).build(),
                HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
            var counts = JsonMapper.builder().build().readTree(response.body()).get("data");
            for (String field : new String[] {"dayVisit", "weekVisit", "monthVisit", "yearVisit"}) {
                assertThat(counts.get(field).asLong()).as(field).isZero();
            }
        }
    }

    private static void declareBlogEvents(GenericContainer<?> rabbit) throws Exception {
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
