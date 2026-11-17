package wiki.chiu.micro.exhibit;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import tools.jackson.databind.json.JsonMapper;

@EnabledIfEnvironmentVariable(named = "MICRO_EXHIBIT_NATIVE_EXECUTABLE", matches = ".+")
class ExhibitNativeExecutableIntegrationTest {

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();

    @Test
    void startsNativeExecutableAndExecutesVisitCountLua() throws Exception {
        try (GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:8-alpine"));
            GenericContainer<?> rabbit = new GenericContainer<>(DockerImageName.parse("rabbitmq:4.3.4-management"))) {
            redis.withExposedPorts(6379).withCommand("redis-server", "--requirepass", "test-password");
            rabbit.withExposedPorts(5672)
                .withEnv("RABBITMQ_DEFAULT_USER", "test")
                .withEnv("RABBITMQ_DEFAULT_PASS", "test-password")
                .waitingFor(Wait.forLogMessage(".*Server startup complete.*\\n", 1));
            redis.start();
            rabbit.start();
            ExhibitNativeTestSupport.declareBlogEvents(rabbit);

            Path output = Files.createTempFile("micro-exhibit-native-startup-", ".log");
            Process application = new ProcessBuilder(
                System.getenv("MICRO_EXHIBIT_NATIVE_EXECUTABLE"),
                "--server.port=0",
                "--spring.data.redis.host=" + redis.getHost(),
                "--spring.data.redis.port=" + redis.getMappedPort(6379),
                "--spring.data.redis.password=test-password",
                "--spring.rabbitmq.host=" + rabbit.getHost(),
                "--spring.rabbitmq.port=" + rabbit.getMappedPort(5672),
                "--spring.rabbitmq.username=test",
                "--spring.rabbitmq.password=test-password",
                "--megalith.blog.existence-index.initial-delay=1h",
                "--management.otlp.metrics.export.enabled=false")
                .redirectErrorStream(true).redirectOutput(output.toFile()).start();
            try {
                int port = awaitPort(application, output);
                var response = get(port, "/public/blog/stat");
                assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
                var counts = JsonMapper.builder().build().readTree(response.body()).get("data");
                for (String field : new String[] {"dayVisit", "weekVisit", "monthVisit", "yearVisit"}) {
                    assertThat(counts.get(field).asLong()).as(field).isZero();
                }
            } finally {
                application.destroy();
                if (!application.waitFor(10, TimeUnit.SECONDS)) {
                    application.destroyForcibly().waitFor(10, TimeUnit.SECONDS);
                }
            }
        }
    }

    private int awaitPort(Process application, Path output) throws Exception {
        var pattern = Pattern.compile("Tomcat started on port ([0-9]+)");
        long deadline = System.nanoTime() + Duration.ofSeconds(45).toNanos();
        while (application.isAlive() && System.nanoTime() < deadline) {
            var match = pattern.matcher(Files.readString(output));
            if (match.find()) {
                int port = Integer.parseInt(match.group(1));
                try {
                    if (get(port, "/actuator/health").statusCode() == 200) {
                        return port;
                    }
                } catch (IOException ignored) {
                    // The port binds before startup has completed.
                }
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Native exhibit failed to become healthy:\n" + Files.readString(output));
    }

    private HttpResponse<String> get(int port, String path) throws IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
            .timeout(Duration.ofSeconds(2)).build(), HttpResponse.BodyHandlers.ofString());
    }
}
