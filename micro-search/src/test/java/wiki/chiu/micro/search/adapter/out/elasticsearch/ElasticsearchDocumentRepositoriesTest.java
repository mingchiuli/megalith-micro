package wiki.chiu.micro.search.adapter.out.elasticsearch;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

class ElasticsearchDocumentRepositoriesTest {

    @Test
    void providesRepositoryMetadataForEveryDocument() {
        assertThat(ElasticsearchRepository.class).isAssignableFrom(BlogDocumentRepository.class);
        assertThat(ElasticsearchRepository.class)
            .isAssignableFrom(AdminBlogDocumentRepository.class);
        assertThat(ElasticsearchRepository.class)
            .isAssignableFrom(AdminRevisionDocumentRepository.class);
    }
}
