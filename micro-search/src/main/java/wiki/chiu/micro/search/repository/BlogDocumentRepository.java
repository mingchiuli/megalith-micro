package wiki.chiu.micro.search.repository;

import wiki.chiu.micro.search.document.BlogDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author mingchiuli
 * @create 2022-12-23 2:11 pm
 */
public interface BlogDocumentRepository extends ElasticsearchRepository<BlogDocument, Long> {}
