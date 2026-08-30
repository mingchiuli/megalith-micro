package wiki.chiu.micro.search.adapter.out.elasticsearch;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author mingchiuli
 * @create 2022-12-23 2:11 pm
 */
public interface BlogDocumentRepository extends ElasticsearchRepository<BlogDocument, Long> {}
