package wiki.chiu.micro.search.adapter.out.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface AdminRevisionDocumentRepository
    extends ElasticsearchRepository<AdminRevisionDocument, Long> {
}
