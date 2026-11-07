package wiki.chiu.micro.blog;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import tools.jackson.databind.json.JsonMapper;
import wiki.chiu.micro.common.auth.web.AuthPrincipalCodec;
import wiki.chiu.micro.common.security.AuthPrincipal;
import wiki.chiu.micro.common.security.InternalHttpHeaders;

@Testcontainers(disabledWithoutDocker = true)
class BlogApplicationStartupIntegrationTest {

    @Container
    private static final GenericContainer<?> DATABASE = databaseContainer();

    @Container
    private static final GenericContainer<?> REDIS = redisContainer();

    @Container
    private static final GenericContainer<?> RABBIT = rabbitContainer();

    private static GenericContainer<?> databaseContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("mariadb:12.3.2"));
        container.addEnv("MARIADB_DATABASE", "blog_startup");
        container.addEnv("MARIADB_ROOT_PASSWORD", "test-password");
        container.addExposedPorts(3306);
        container.waitingFor(Wait.forLogMessage(".*ready for connections.*\\n", 2));
        return container;
    }

    private static GenericContainer<?> redisContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("redis:8-alpine"));
        container.addExposedPorts(6379);
        return container;
    }

    private static GenericContainer<?> rabbitContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("rabbitmq:4.3.4-management"));
        container.addEnv("RABBITMQ_DEFAULT_USER", "test");
        container.addEnv("RABBITMQ_DEFAULT_PASS", "test-password");
        container.addExposedPorts(5672);
        container.waitingFor(Wait.forLogMessage(".*Server startup complete.*\\n", 1));
        return container;
    }

    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void startsTheActualApplicationAndInitializesCollaborationThroughTheConfiguredClient() throws Exception {
        try (var downstream = new Downstream()) {
            try (var context = new SpringApplicationBuilder(MicroBlogApplication.class)
                .run(arguments(downstream.port()))) {
                assertThat(context.getBeansOfType(RestClient.Builder.class)).isNotEmpty();
                int port = ((WebServerApplicationContext) context).getWebServer().getPort();
                verifyEndpoints(port, downstream);
            }
        }
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "MICRO_BLOG_NATIVE_EXECUTABLE", matches = ".+")
    void startsTheNativeExecutableAndInitializesCollaboration() throws Exception {
        try (var downstream = new Downstream()) {
            Path output = Files.createTempFile("micro-blog-native-startup-", ".log");
            var command = new ArrayList<String>();
            command.add(System.getenv("MICRO_BLOG_NATIVE_EXECUTABLE"));
            command.addAll(List.of(arguments(downstream.port())));
            Process process = new ProcessBuilder(command).redirectErrorStream(true).redirectOutput(output.toFile()).start();
            try {
                int port = awaitNativePort(process, output);
                verifyEndpoints(port, downstream);
            } finally {
                process.destroy();
                if (!process.waitFor(10, TimeUnit.SECONDS)) {
                    process.destroyForcibly().waitFor(10, TimeUnit.SECONDS);
                }
            }
        }
    }

    private int awaitNativePort(Process process, Path output) throws Exception {
        var pattern = Pattern.compile("Tomcat started on port ([0-9]+)");
        long deadline = System.nanoTime() + Duration.ofSeconds(45).toNanos();
        while (process.isAlive() && System.nanoTime() < deadline) {
            var match = pattern.matcher(Files.readString(output));
            if (match.find()) {
                int port = Integer.parseInt(match.group(1));
                try {
                    if (client.send(HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/actuator/health"))
                        .timeout(Duration.ofSeconds(2)).build(), HttpResponse.BodyHandlers.discarding()).statusCode() == 200) {
                        return port;
                    }
                } catch (IOException ignored) {
                    // Tomcat binds before the application has finished starting.
                }
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Native application failed to become healthy:\n" + Files.readString(output));
    }

    private void verifyEndpoints(int port, Downstream downstream) throws Exception {
        String base = "http://127.0.0.1:" + port;
        var health = client.send(HttpRequest.newBuilder(URI.create(base + "/actuator/health"))
            .timeout(Duration.ofSeconds(5)).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(health.statusCode()).isEqualTo(200);
        String principal = AuthPrincipalCodec.encode(new AuthPrincipal(42L, List.of("user"), List.of()));
        var created = client.send(HttpRequest.newBuilder(URI.create(base + "/sys/blog/draft/new"))
            .header(InternalHttpHeaders.PRINCIPAL, principal)
            .POST(HttpRequest.BodyPublishers.noBody()).timeout(Duration.ofSeconds(5)).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(created.statusCode()).as(created.body()).isEqualTo(200);
        var identity = json.readTree(created.body()).get("data");
        long blogId = identity.get("id").asLong();
        String epoch = identity.get("collaborationEpoch").asString();
        long deadline = System.currentTimeMillis() + 45_000;
        var response = client.send(HttpRequest.newBuilder(URI.create(base + "/sys/blog/edit/ticket?blogId=" + blogId))
            .header(InternalHttpHeaders.PRINCIPAL, principal)
            .header(InternalHttpHeaders.AUTHORIZATION_EXPIRES_AT, Long.toString(deadline))
            .POST(HttpRequest.BodyPublishers.noBody()).timeout(Duration.ofSeconds(10)).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
        var ticket = json.readTree(response.body()).get("data");
        assertThat(ticket.get("ticket").asString()).isEqualTo("Bearer integration-ticket");
        assertThat(ticket.get("roomId").asString()).isEqualTo("blog:" + blogId + ":" + epoch);
        assertThat(ticket.get("expiresAt").asLong()).isLessThanOrEqualTo(deadline);
        assertThat(downstream.room.get()).isEqualTo("/inner/rooms/blog:" + blogId + ":" + epoch + "/initialize");
        assertThat(json.readTree(downstream.content.get()).get("content").asString()).isEmpty();
        assertThat(downstream.traceparent.get()).isNotBlank();
        assertThat(json.readTree(downstream.ticket.get()).get("roomId").asString()).isEqualTo(ticket.get("roomId").asString());
        verifyRecycleRoundTrip(base, principal, blogId);
    }

    private void verifyRecycleRoundTrip(String base, String principal, long blogId) throws Exception {
        var deletion = client.send(HttpRequest.newBuilder(URI.create(base + "/sys/blog/delete"))
            .header(InternalHttpHeaders.PRINCIPAL, principal)
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("[" + blogId + "]"))
            .timeout(Duration.ofSeconds(5)).build(), HttpResponse.BodyHandlers.ofString());
        assertThat(deletion.statusCode()).as(deletion.body()).isEqualTo(200);
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        while (System.nanoTime() < deadline) {
            var response = client.send(HttpRequest.newBuilder(URI.create(base + "/sys/blog/deleted?currentPage=1&size=100"))
                .header(InternalHttpHeaders.PRINCIPAL, principal)
                .timeout(Duration.ofSeconds(5)).build(), HttpResponse.BodyHandlers.ofString());
            assertThat(response.statusCode()).as(response.body()).isEqualTo(200);
            var rows = json.readTree(response.body()).get("data").get("content");
            for (int index = 0; index < rows.size(); index++) {
                if (rows.get(index).get("id").asLong() == blogId) {
                    assertThat(rows.get(index).get("userId").asLong()).isEqualTo(42L);
                    return;
                }
            }
            Thread.sleep(100);
        }
        throw new AssertionError("Deleted blog did not reach the recycle bin through the outbox and RabbitMQ");
    }

    private String[] arguments(int downstreamPort) {
        String downstream = "http://127.0.0.1:" + downstreamPort;
        return new String[] {
            "--server.port=0",
            "--spring.datasource.url=jdbc:mariadb://" + DATABASE.getHost() + ":" + DATABASE.getMappedPort(3306) + "/blog_startup",
            "--spring.datasource.username=root", "--spring.datasource.password=test-password",
            "--spring.data.redis.host=" + REDIS.getHost(), "--spring.data.redis.port=" + REDIS.getMappedPort(6379),
            "--spring.data.redis.password=",
            "--spring.rabbitmq.host=" + RABBIT.getHost(), "--spring.rabbitmq.port=" + RABBIT.getMappedPort(5672),
            "--spring.rabbitmq.username=test", "--spring.rabbitmq.password=test-password",
            "--megalith.blog.sync-url=" + downstream,
            "--megalith.blog.auth-url=" + downstream + "/inner",
            "--megalith.blog.statistics-sync.enabled=false",
            "--management.otlp.metrics.export.enabled=false",
            "--management.opentelemetry.tracing.export.otlp.endpoint=" + downstream + "/v1/traces",
            "--management.opentelemetry.logging.export.otlp.endpoint=" + downstream + "/v1/logs"
        };
    }

    private static final class Downstream implements AutoCloseable {
        private final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        private final AtomicReference<String> room = new AtomicReference<>();
        private final AtomicReference<String> content = new AtomicReference<>();
        private final AtomicReference<String> traceparent = new AtomicReference<>();
        private final AtomicReference<String> ticket = new AtomicReference<>();

        private Downstream() throws IOException {
            server.createContext("/inner/rooms/", exchange -> {
                room.set(exchange.getRequestURI().getPath());
                traceparent.set(exchange.getRequestHeaders().getFirst("traceparent"));
                content.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            });
            server.createContext("/inner/token/websocket", exchange -> {
                ticket.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                byte[] body = "{\"code\":200,\"msg\":\"success\",\"data\":\"Bearer integration-ticket\"}".getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.createContext("/v1/", exchange -> {
                exchange.getRequestBody().readAllBytes();
                exchange.sendResponseHeaders(200, -1);
                exchange.close();
            });
            server.start();
        }

        private int port() {
            return server.getAddress().getPort();
        }

        @Override
        public void close() {
            server.stop(0);
        }
    }
}
