package wiki.chiu.micro.search.adapter.out.meilisearch;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public final class MeilisearchClient {

    private final RestClient http;
    private final JsonMapper json;
    private final Duration taskTimeout;

    public MeilisearchClient(RestClient http, JsonMapper json, Duration taskTimeout) {
        this.http = http;
        this.json = json;
        this.taskTimeout = taskTimeout;
    }

    JsonNode request(HttpMethod method, String path, Object body) {
        var request = http.method(method).uri(path);
        if (body != null) {
            request.contentType(MediaType.APPLICATION_JSON).body(json.writeValueAsString(body));
        }
        return json.readTree(request.retrieve().body(String.class));
    }

    JsonNode lookup(String path, String missingCode) {
        try {
            return request(HttpMethod.GET, path, null);
        } catch (HttpClientErrorException.NotFound e) {
            if (missingCode.equals(json.readTree(e.getResponseBodyAsString()).path("code").asString())) {
                return null;
            }
            throw e;
        }
    }

    JsonNode document(String index, long id) {
        return lookup("/indexes/" + index + "/documents/" + id, "document_not_found");
    }

    void replace(String index, List<?> docs) {
        if (!docs.isEmpty()) {
            task(HttpMethod.POST, "/indexes/" + index + "/documents", docs);
        }
    }

    JsonNode search(String index, Map<String, Object> query) {
        return request(HttpMethod.POST, "/indexes/" + index + "/search", query);
    }

    void task(HttpMethod method, String path, Object body) {
        await(request(method, path, body).required("taskUid").asLong());
    }

    // Recover an interrupted writer before comparing the stored revision.
    void drain(String index) {
        var pending = request(HttpMethod.GET,
            "/tasks?indexUids=" + index + "&statuses=enqueued,processing&limit=1", null);
        for (JsonNode task : pending.required("results")) {
            await(task.required("uid").asLong());
        }
    }

    private void await(long uid) {
        long start = System.nanoTime();
        while (System.nanoTime() - start < taskTimeout.toNanos()) {
            var task = request(HttpMethod.GET, "/tasks/" + uid, null);
            switch (task.required("status").asString()) {
                case "succeeded" -> { return; }
                case "failed", "canceled" -> throw new IllegalStateException(
                    "Meilisearch task " + uid + " failed: " + task.path("error"));
                default -> {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Interrupted waiting for Meilisearch task " + uid, e);
                    }
                }
            }
        }
        throw new IllegalStateException("Meilisearch task " + uid + " is still pending");
    }
}
