package wiki.chiu.micro.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import wiki.chiu.micro.search.document.BlogDocument;

/**
 * @author mingchiuli
 * @create 2022-12-23 2:11 pm
 */
public interface BlogDocumentRepository extends ElasticsearchRepository<BlogDocument, Long> {}
