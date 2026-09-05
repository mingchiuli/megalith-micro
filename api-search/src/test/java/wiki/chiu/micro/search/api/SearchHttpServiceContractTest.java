package wiki.chiu.micro.search.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.service.annotation.PostExchange;

class SearchHttpServiceContractTest {

    @Test
    void statisticsUseAnExplicitBatchResource() throws NoSuchMethodException {
        var method = SearchHttpService.class.getMethod("updateReadCounts", List.class);

        assertThat(method.getAnnotation(PostExchange.class).value()).isEqualTo("/blog/views/batch");
    }
}
