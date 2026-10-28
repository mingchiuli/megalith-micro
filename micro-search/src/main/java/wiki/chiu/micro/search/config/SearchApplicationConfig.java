package wiki.chiu.micro.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

import wiki.chiu.micro.search.adapter.out.elasticsearch.ElasticsearchBlogAdapter;
import wiki.chiu.micro.search.adapter.out.elasticsearch.ElasticsearchIndexMaintenanceAdapter;
import wiki.chiu.micro.search.adapter.out.elasticsearch.ElasticsearchSearchIndexSchemaAdapter;
import wiki.chiu.micro.search.application.port.in.ApplyBlogIndexChangeUseCase;
import wiki.chiu.micro.search.application.port.in.BootstrapSearchIndexes;
import wiki.chiu.micro.search.application.port.in.RebuildSearchIndex;
import wiki.chiu.micro.search.application.port.in.SearchBlogsUseCase;
import wiki.chiu.micro.search.application.port.out.BlogBootstrapIndexSource;
import wiki.chiu.micro.search.application.port.out.BlogIndexMaintenance;
import wiki.chiu.micro.search.application.port.out.BlogIndexSource;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;
import wiki.chiu.micro.search.application.port.out.SearchRebuildControl;
import wiki.chiu.micro.search.application.port.out.SearchIndexSchema;
import wiki.chiu.micro.search.application.service.BlogIndexApplicationService;
import wiki.chiu.micro.search.application.service.IndexRebuildService;
import wiki.chiu.micro.search.application.service.SearchApplicationService;
import wiki.chiu.micro.search.application.service.SearchIndexBootstrapService;

@Configuration(proxyBeanMethods = false)
public class SearchApplicationConfig {

    @Bean
    ElasticsearchBlogAdapter elasticsearchBlogAdapter(
        ElasticsearchTemplate elasticsearchTemplate,
        @Value("${megalith.blog.blog-page-size}") int publicPageSize,
        @Value("${megalith.search.index.alias:blog_search}") String alias,
        @Value("${megalith.search.index.admin-alias:blog_admin_search}") String adminAlias,
        @Value("${megalith.search.index.revision-alias:blog_revision_search}")
            String revisionAlias) {
        return new ElasticsearchBlogAdapter(
            elasticsearchTemplate,
            publicPageSize,
            alias,
            adminAlias,
            revisionAlias);
    }

    @Bean
    ElasticsearchIndexMaintenanceAdapter indexMaintenance(
        ElasticsearchClient client, ElasticsearchTemplate template,
        @Value("${megalith.search.index.alias:blog_search}") String alias) {
        return new ElasticsearchIndexMaintenanceAdapter(client, template, alias);
    }

    @Bean
    SearchIndexSchema searchIndexSchema(
        ElasticsearchClient client,
        ElasticsearchTemplate template,
        @Value("${megalith.search.index.alias:blog_search}") String alias,
        @Value("${megalith.search.index.admin-alias:blog_admin_search}") String adminAlias,
        @Value("${megalith.search.index.revision-alias:blog_revision_search}")
            String revisionAlias) {
        return new ElasticsearchSearchIndexSchemaAdapter(
            client, template, alias, adminAlias, revisionAlias);
    }

    @Bean
    BootstrapSearchIndexes bootstrapSearchIndexes(
        SearchIndexSchema schema,
        BlogBootstrapIndexSource source,
        BlogIndexWriter writer) {
        return new SearchIndexBootstrapService(schema, source, writer);
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
