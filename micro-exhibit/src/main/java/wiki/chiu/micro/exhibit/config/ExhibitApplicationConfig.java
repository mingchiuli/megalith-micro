package wiki.chiu.micro.exhibit.config;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;
import wiki.chiu.micro.exhibit.application.port.in.BlogService;
import wiki.chiu.micro.exhibit.application.port.in.VisitStatisticsMaintenance;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;
import wiki.chiu.micro.exhibit.application.port.out.BlogReader;
import wiki.chiu.micro.exhibit.application.port.out.ExhibitMetrics;
import wiki.chiu.micro.exhibit.application.port.out.ExistenceIndexMetrics;
import wiki.chiu.micro.exhibit.application.port.out.SensitiveContentReader;
import wiki.chiu.micro.exhibit.application.port.out.VisitStatisticsStore;
import wiki.chiu.micro.exhibit.application.service.BlogExistenceServiceImpl;
import wiki.chiu.micro.exhibit.application.service.BlogServiceImpl;
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
}
