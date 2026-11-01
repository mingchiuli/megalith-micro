package wiki.chiu.micro.search.adapter.out.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

class RecommendationCursorCodecTest {

    private final RecommendationCursorCodec codec =
        new RecommendationCursorCodec(JsonMapper.builder().build());

    @Test
    void roundTripsAnOpaqueVersionedCursor() {
        RecommendationCursor cursor =
            new RecommendationCursor(1, "pit-id", 1_800_000_000_000L, 7.25, 42L);

        String encoded = codec.encode(cursor);

        assertThat(encoded).doesNotContain("pit-id");
        assertThat(codec.decode(encoded)).isEqualTo(cursor);
    }

    @Test
    void rejectsMalformedAndUnsupportedCursors() {
        assertThatThrownBy(() -> codec.decode("not-base64"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("invalid recommendation cursor");

        String unsupported =
            codec.encode(new RecommendationCursor(2, "pit-id", 1L, 1.0, 1L));
        assertThatThrownBy(() -> codec.decode(unsupported))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
