package wiki.chiu.micro.search.adapter.out.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface BlogDocumentRepository extends ElasticsearchRepository<BlogDocument, Long> {
}
