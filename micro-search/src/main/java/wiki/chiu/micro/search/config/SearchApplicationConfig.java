package wiki.chiu.micro.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;

import io.micrometer.core.instrument.MeterRegistry;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.search.adapter.out.elasticsearch.AliasInitializingElasticsearchRepository;
import wiki.chiu.micro.search.adapter.out.elasticsearch.ElasticsearchBlogIndexWriter;
import wiki.chiu.micro.search.adapter.out.elasticsearch.ElasticsearchBlogQueryAdapter;
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
@EnableElasticsearchRepositories(
    basePackages = "wiki.chiu.micro.search.adapter.out.elasticsearch",
    repositoryBaseClass = AliasInitializingElasticsearchRepository.class)
public class SearchApplicationConfig {

    @Bean
    ElasticsearchBlogQueryAdapter elasticsearchBlogQueryAdapter(
        ElasticsearchTemplate elasticsearchTemplate,
        @Value("${megalith.blog.blog-page-size}") int publicPageSize,
        @Value("${megalith.search.index.alias:blog_search}") String alias,
        @Value("${megalith.search.index.admin-alias:blog_admin_search}") String adminAlias,
        @Value("${megalith.search.index.revision-alias:blog_revision_search}")
            String revisionAlias,
        @Value("${megalith.search.recommendation.pit-keep-alive:2m}")
            Duration recommendationPitKeepAlive,
        JsonMapper jsonMapper,
        MeterRegistry meterRegistry) {
        return new ElasticsearchBlogQueryAdapter(
            elasticsearchTemplate,
            publicPageSize,
            alias,
            adminAlias,
            revisionAlias,
            recommendationPitKeepAlive,
            jsonMapper,
            meterRegistry);
    }

    @Bean
    ElasticsearchBlogIndexWriter elasticsearchBlogIndexWriter(
        ElasticsearchTemplate elasticsearchTemplate,
        @Value("${megalith.search.index.alias:blog_search}") String alias,
        @Value("${megalith.search.index.admin-alias:blog_admin_search}") String adminAlias,
        @Value("${megalith.search.index.revision-alias:blog_revision_search}")
            String revisionAlias) {
        return new ElasticsearchBlogIndexWriter(
            elasticsearchTemplate, alias, adminAlias, revisionAlias);
    }

    @Bean
    ElasticsearchIndexMaintenanceAdapter indexMaintenance(
        ElasticsearchClient client, ElasticsearchTemplate template,
        @Value("${megalith.search.index.alias:blog_search}") String alias,
        @Value("${megalith.search.index.admin-alias:blog_admin_search}") String adminAlias,
        @Value("${megalith.search.index.revision-alias:blog_revision_search}")
            String revisionAlias) {
        return new ElasticsearchIndexMaintenanceAdapter(
            client, template, alias, adminAlias, revisionAlias);
    }

    @Bean
    RebuildSearchIndex rebuildSearchIndex(
        BlogIndexSource source, BlogIndexMaintenance indexes, SearchRebuildControl control,
        @Value("${megalith.search.index.rebuild-batch-size:500}") int batchSize) {
        return new IndexRebuildService(source, indexes, control, batchSize);
    }

    @Bean
    SearchBlogsUseCase searchBlogsUseCase(
        BlogSearchIndex searchIndex, BlogIndexWriter indexWriter) {
        return new SearchApplicationService(searchIndex, indexWriter);
    }

    @Bean
    ApplyBlogIndexChangeUseCase applyBlogIndexChangeUseCase(BlogIndexWriter indexWriter) {
        return new BlogIndexApplicationService(indexWriter);
    }
}
