package wiki.chiu.micro.search.adapter.out.elasticsearch;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import tools.jackson.databind.json.JsonMapper;

final class RecommendationCursorCodec {

    private static final int VERSION = 1;
    private static final int MAX_ENCODED_LENGTH = 4096;

    private final JsonMapper jsonMapper;

    RecommendationCursorCodec(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    String encode(RecommendationCursor cursor) {
        byte[] json = jsonMapper.writeValueAsBytes(cursor);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
    }

    RecommendationCursor decode(String encoded) {
        if (encoded == null || encoded.isBlank() || encoded.length() > MAX_ENCODED_LENGTH) {
            throw invalidCursor();
        }
        try {
            byte[] json = Base64.getUrlDecoder().decode(encoded);
            RecommendationCursor cursor =
                jsonMapper.readValue(new String(json, StandardCharsets.UTF_8), RecommendationCursor.class);
            if (cursor.version() != VERSION
                || cursor.pitId() == null
                || cursor.pitId().isBlank()
                || cursor.score() < 0
                || !Double.isFinite(cursor.score())
                || cursor.id() < 1
                || cursor.snapshotEpochMillis() < 1) {
                throw invalidCursor();
            }
            return cursor;
        } catch (RuntimeException failure) {
            throw invalidCursor();
        }
    }

    private static IllegalArgumentException invalidCursor() {
        return new IllegalArgumentException("invalid recommendation cursor");
    }
}
