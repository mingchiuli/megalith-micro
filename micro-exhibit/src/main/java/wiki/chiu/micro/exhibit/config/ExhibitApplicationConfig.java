package wiki.chiu.micro.exhibit.config;

import java.time.Clock;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import wiki.chiu.micro.exhibit.application.port.in.BlogChangeMaintenance;
import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;
import wiki.chiu.micro.exhibit.application.port.in.BlogService;
import wiki.chiu.micro.exhibit.application.port.in.VisitStatisticsMaintenance;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;
import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;
import wiki.chiu.micro.exhibit.application.port.out.BlogReader;
import wiki.chiu.micro.exhibit.application.port.out.ExhibitMetrics;
import wiki.chiu.micro.exhibit.application.port.out.ExistenceIndexMetrics;
import wiki.chiu.micro.exhibit.application.port.out.VisitStatisticsStore;
import wiki.chiu.micro.exhibit.application.service.BlogChangeMaintenanceHandler;
import wiki.chiu.micro.exhibit.application.service.BlogChangeMaintenanceServiceImpl;
import wiki.chiu.micro.exhibit.application.service.BlogExistenceServiceImpl;
import wiki.chiu.micro.exhibit.application.service.BlogServiceImpl;
import wiki.chiu.micro.exhibit.application.service.CreateBlogChangeMaintenanceHandler;
import wiki.chiu.micro.exhibit.application.service.DeleteBlogChangeMaintenanceHandler;
import wiki.chiu.micro.exhibit.application.service.UpdateBlogChangeMaintenanceHandler;
import wiki.chiu.micro.exhibit.application.service.VisitStatisticsMaintenanceService;

/**
 * Wires the use cases to the adapters that implement their ports. The application layer carries no
 * Spring annotations, so every service bean is declared here.
 */
@Configuration(proxyBeanMethods = false)
public class ExhibitApplicationConfig {

    @Bean
    BlogService blogService(
        BlogReader blogReader,
        ExhibitMetrics metrics) {
        return new BlogServiceImpl(blogReader, metrics);
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
    CreateBlogChangeMaintenanceHandler createBlogChangeMaintenanceHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogExistenceStore existence) {
        return new CreateBlogChangeMaintenanceHandler(revisionGuard, existence);
    }

    @Bean
    UpdateBlogChangeMaintenanceHandler updateBlogChangeMaintenanceHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogReadStateStore readStateStore) {
        return new UpdateBlogChangeMaintenanceHandler(
            revisionGuard, readStateStore);
    }

    @Bean
    DeleteBlogChangeMaintenanceHandler deleteBlogChangeMaintenanceHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogReadStateStore readStateStore,
        BlogExistenceStore existence) {
        return new DeleteBlogChangeMaintenanceHandler(
            revisionGuard, readStateStore, existence);
    }

    @Bean
    BlogChangeMaintenance blogChangeMaintenance(List<BlogChangeMaintenanceHandler> handlers) {
        return new BlogChangeMaintenanceServiceImpl(handlers);
    }
}
