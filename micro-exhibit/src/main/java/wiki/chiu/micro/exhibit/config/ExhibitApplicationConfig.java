package wiki.chiu.micro.exhibit.config;

import java.time.Clock;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import wiki.chiu.micro.exhibit.application.port.in.BlogChangeEviction;
import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;
import wiki.chiu.micro.exhibit.application.port.in.BlogService;
import wiki.chiu.micro.exhibit.application.port.in.VisitStatisticsMaintenance;
import wiki.chiu.micro.exhibit.application.port.out.BlogCacheInvalidation;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;
import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;
import wiki.chiu.micro.exhibit.application.port.out.BlogReader;
import wiki.chiu.micro.exhibit.application.port.out.ExhibitMetrics;
import wiki.chiu.micro.exhibit.application.port.out.ExistenceIndexMetrics;
import wiki.chiu.micro.exhibit.application.port.out.SensitiveContentReader;
import wiki.chiu.micro.exhibit.application.port.out.VisitStatisticsStore;
import wiki.chiu.micro.exhibit.application.service.BlogChangeEvictionHandler;
import wiki.chiu.micro.exhibit.application.service.BlogChangeEvictionServiceImpl;
import wiki.chiu.micro.exhibit.application.service.BlogExistenceServiceImpl;
import wiki.chiu.micro.exhibit.application.service.BlogServiceImpl;
import wiki.chiu.micro.exhibit.application.service.CreateBlogChangeEvictionHandler;
import wiki.chiu.micro.exhibit.application.service.DeleteBlogChangeEvictionHandler;
import wiki.chiu.micro.exhibit.application.service.UpdateBlogChangeEvictionHandler;
import wiki.chiu.micro.exhibit.application.service.VisitStatisticsMaintenanceService;

/**
 * Wires the use cases to the adapters that implement their ports. The application layer carries no
 * Spring annotations, so every service bean is declared here.
 */
@Configuration(proxyBeanMethods = false)
public class ExhibitApplicationConfig {

    @Bean
    BlogService blogService(
        SensitiveContentReader sensitiveContentReader,
        BlogCatalog blogCatalog,
        BlogReader blogReader,
        ExhibitMetrics metrics) {
        return new BlogServiceImpl(sensitiveContentReader, blogCatalog, blogReader, metrics);
    }

    @Bean
    BlogExistenceService blogExistenceService(
        BlogExistenceStore store,
        BlogCatalog catalog,
        @Value("${megalith.blog.existence-index.batch-size:1000}") int batchSize,
        ExistenceIndexMetrics metrics) {
        return new BlogExistenceServiceImpl(store, catalog, batchSize, metrics);
    }

    @Bean
    VisitStatisticsMaintenance visitStatisticsMaintenance(VisitStatisticsStore store) {
        return new VisitStatisticsMaintenanceService(store, Clock.systemDefaultZone());
    }

    @Bean
    CreateBlogChangeEvictionHandler createBlogChangeEvictionHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogCacheInvalidation cacheInvalidation,
        BlogExistenceStore existence) {
        return new CreateBlogChangeEvictionHandler(revisionGuard, cacheInvalidation, existence);
    }

    @Bean
    UpdateBlogChangeEvictionHandler updateBlogChangeEvictionHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogCacheInvalidation cacheInvalidation,
        BlogReadStateStore readStateStore) {
        return new UpdateBlogChangeEvictionHandler(
            revisionGuard, cacheInvalidation, readStateStore);
    }

    @Bean
    DeleteBlogChangeEvictionHandler deleteBlogChangeEvictionHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogCacheInvalidation cacheInvalidation,
        BlogReadStateStore readStateStore,
        BlogExistenceStore existence) {
        return new DeleteBlogChangeEvictionHandler(
            revisionGuard, cacheInvalidation, readStateStore, existence);
    }

    @Bean
    BlogChangeEviction blogChangeEviction(List<BlogChangeEvictionHandler> handlers) {
        return new BlogChangeEvictionServiceImpl(handlers);
    }
}
