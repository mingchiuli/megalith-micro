package wiki.chiu.micro.search.application.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import wiki.chiu.micro.common.exception.ValidationException;
import wiki.chiu.micro.search.application.model.RecommendationFeedQuery;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;

class SearchApplicationServiceTest {

    private final BlogSearchIndex searchIndex = mock(BlogSearchIndex.class);
    private final SearchApplicationService service =
        new SearchApplicationService(searchIndex, mock(BlogIndexWriter.class));

    @Test
    void mapsAnInvalidRecommendationCursorToValidationFailure() {
        RecommendationFeedQuery query = new RecommendationFeedQuery("invalid", 5);
        when(searchIndex.recommendationFeed(query))
            .thenThrow(new IllegalArgumentException("invalid recommendation cursor"));

        assertThatThrownBy(() -> service.recommendationFeed(query))
            .isInstanceOf(ValidationException.class)
            .hasMessage("invalid recommendation cursor");
    }

    @Test
    void preservesUnexpectedIndexingFailures() {
        RecommendationFeedQuery query = new RecommendationFeedQuery(null, 5);
        IllegalArgumentException failure = new IllegalArgumentException("query failed");
        when(searchIndex.recommendationFeed(query)).thenThrow(failure);

        assertThatThrownBy(() -> service.recommendationFeed(query))
            .isSameAs(failure);
    }
}
