package wiki.chiu.micro.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

import wiki.chiu.micro.scheduling.RedisTaskLock;
import wiki.chiu.micro.search.adapter.out.elasticsearch.ElasticsearchBlogAdapter;
import wiki.chiu.micro.search.adapter.out.elasticsearch.ElasticsearchIndexMaintenanceAdapter;
import wiki.chiu.micro.search.application.port.in.ApplyBlogIndexChangeUseCase;
import wiki.chiu.micro.search.application.port.in.RebuildSearchIndex;
import wiki.chiu.micro.search.application.port.in.SearchBlogsUseCase;
import wiki.chiu.micro.search.application.port.out.BlogIndexMaintenance;
import wiki.chiu.micro.search.application.port.out.BlogIndexSource;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;
import wiki.chiu.micro.search.application.port.out.SearchRebuildControl;
import wiki.chiu.micro.search.application.service.BlogIndexApplicationService;
import wiki.chiu.micro.search.application.service.IndexRebuildService;
import wiki.chiu.micro.search.application.service.SearchApplicationService;

@Configuration(proxyBeanMethods = false)
public class SearchApplicationConfig {

    @Bean
    ElasticsearchBlogAdapter elasticsearchBlogAdapter(
        ElasticsearchTemplate elasticsearchTemplate,
        @Value("${megalith.blog.blog-page-size}") int publicPageSize,
        @Value("${megalith.search.index.alias:blog_search}") String alias) {
        return new ElasticsearchBlogAdapter(elasticsearchTemplate, publicPageSize, alias);
    }

    @Bean
    ElasticsearchIndexMaintenanceAdapter indexMaintenance(
        ElasticsearchClient client, ElasticsearchTemplate template,
        @Value("${megalith.search.index.alias:blog_search}") String alias,
        @Value("${megalith.search.index.legacy-name:blog_index_v4}") String legacyIndex) {
        return new ElasticsearchIndexMaintenanceAdapter(client, template, alias, legacyIndex);
    }

    @Bean
    ApplicationRunner initializeSearchAlias(ElasticsearchIndexMaintenanceAdapter indexes, RedisTaskLock lock) {
        return args -> lock.run("search:index-rebuild", () -> {
            indexes.ensureAlias();
            return null;
        });
    }

    @Bean
    RebuildSearchIndex rebuildSearchIndex(
        BlogIndexSource source, BlogIndexMaintenance indexes, SearchRebuildControl control,
        @Value("${megalith.search.index.rebuild-batch-size:500}") int batchSize) {
        return new IndexRebuildService(source, indexes, control, batchSize);
    }

    @Bean
    SearchBlogsUseCase searchBlogsUseCase(BlogSearchIndex searchIndex) {
        return new SearchApplicationService(searchIndex);
    }

    @Bean
    ApplyBlogIndexChangeUseCase applyBlogIndexChangeUseCase(BlogIndexWriter indexWriter) {
        return new BlogIndexApplicationService(indexWriter);
    }
}
