package wiki.chiu.micro.search.config;

import java.time.Duration;

import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.common.scheduling.RedisTaskLock;
import wiki.chiu.micro.search.adapter.out.meilisearch.MeilisearchBlogIndexWriter;
import wiki.chiu.micro.search.adapter.out.meilisearch.MeilisearchBlogQueryAdapter;
import wiki.chiu.micro.search.adapter.out.meilisearch.MeilisearchClient;
import wiki.chiu.micro.search.adapter.out.meilisearch.MeilisearchIndexes;
import wiki.chiu.micro.search.adapter.out.meilisearch.RecommendationSnapshots;
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
    MeilisearchClient meilisearchClient(RestClient.Builder builder, JsonMapper json,
        @Value("${megalith.search.meilisearch.url:http://127.0.0.1:7700}") String url,
        @Value("${megalith.search.meilisearch.key:${MEILI_MASTER_KEY:}}") String key,
        @Value("${megalith.search.meilisearch.task-timeout:2m}") Duration timeout) {
        if (!key.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + key);
        }
        return new MeilisearchClient(builder.baseUrl(url).build(), json, timeout);
    }

    @Bean
    MeilisearchIndexes indexes(MeilisearchClient client,
        @Value("${megalith.search.index.alias:blog_search}") String blogs,
        @Value("${megalith.search.index.admin-alias:blog_admin_search}") String admin,
        @Value("${megalith.search.index.revision-alias:blog_revision_search}") String revisions,
        @Value("${megalith.search.index.max-total-hits:1000000}") int maxHits) {
        return new MeilisearchIndexes(client, blogs, admin, revisions, maxHits);
    }

    @Bean
    SmartInitializingSingleton initializeIndexes(MeilisearchIndexes indexes, RedisTaskLock lock) {
        return () -> lock.run("search:index-rebuild", () -> {
            indexes.initialize();
            return null;
        });
    }

    @Bean
    MeilisearchBlogIndexWriter blogIndexWriter(MeilisearchClient client, MeilisearchIndexes indexes,
        RedissonClient redis, JsonMapper json) {
        return new MeilisearchBlogIndexWriter(client, indexes, redis, json);
    }

    @Bean
    BlogSearchIndex blogSearchIndex(MeilisearchClient client, MeilisearchIndexes indexes, RedissonClient redis,
        JsonMapper json, @Value("${megalith.blog.blog-page-size}") int size,
        @Value("${megalith.search.recommendation.snapshot-ttl:2m}") Duration ttl) {
        var snapshots = new RecommendationSnapshots(client, indexes, redis, json, ttl);
        return new MeilisearchBlogQueryAdapter(client, indexes, snapshots, json, size);
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
