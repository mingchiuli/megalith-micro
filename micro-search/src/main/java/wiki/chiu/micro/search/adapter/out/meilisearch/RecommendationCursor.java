package wiki.chiu.micro.search.adapter.out.meilisearch;

import java.util.UUID;

public record RecommendationCursor(int version, UUID snapshot, int offset) {
}
