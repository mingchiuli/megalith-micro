package wiki.chiu.micro.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import wiki.chiu.micro.blog.application.port.in.BlogAssetService;
import wiki.chiu.micro.blog.application.port.in.BlogCollaborationService;
import wiki.chiu.micro.blog.application.port.in.BlogExportService;
import wiki.chiu.micro.blog.application.port.in.BlogIndexSourceQueries;
import wiki.chiu.micro.blog.application.port.in.BlogQueryService;
import wiki.chiu.micro.blog.application.port.in.BlogRecycleBin;
import wiki.chiu.micro.blog.application.port.in.BlogSensitiveService;
import wiki.chiu.micro.blog.application.port.in.BlogService;
import wiki.chiu.micro.blog.application.port.in.BlogStatisticsSync;
import wiki.chiu.micro.blog.application.port.out.BlogAssetStorage;
import wiki.chiu.micro.blog.application.port.out.BlogIndexSourceState;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.BlogStatisticsGateway;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.application.port.out.CollaborationTicketGateway;
import wiki.chiu.micro.blog.application.service.BlogAccessPolicy;
import wiki.chiu.micro.blog.application.service.BlogAssetServiceImpl;
import wiki.chiu.micro.blog.application.service.BlogCollaborationServiceImpl;
import wiki.chiu.micro.blog.application.service.BlogExportServiceImpl;
import wiki.chiu.micro.blog.application.service.BlogIndexSourceService;
import wiki.chiu.micro.blog.application.service.BlogQueryServiceImpl;
import wiki.chiu.micro.blog.application.service.BlogRecycleBinServiceImpl;
import wiki.chiu.micro.blog.application.service.BlogSensitiveServiceImpl;
import wiki.chiu.micro.blog.application.service.BlogServiceImpl;
import wiki.chiu.micro.blog.application.service.BlogStatisticsSyncService;

/**
 * Wires the use cases to the adapters that implement their ports. The application layer carries no
 * Spring annotations, so every service bean is declared here.
 */
@Configuration(proxyBeanMethods = false)
public class BlogApplicationConfig {

    @Bean
    BlogAccessPolicy blogAccessPolicy() {
        return new BlogAccessPolicy();
    }

    @Bean
    BlogService blogService(
        BlogQueryStore blogs,
        BlogRuntimeStore runtimeStore,
        BlogWriter writer,
        BlogSearchGateway search,
        BlogAccessPolicy accessPolicy) {
        return new BlogServiceImpl(blogs, runtimeStore, writer, search, accessPolicy);
    }

    @Bean
    BlogQueryService blogQueryService(BlogQueryStore blogs, BlogWriter writer) {
        return new BlogQueryServiceImpl(blogs, writer);
    }

    @Bean
    BlogSensitiveService blogSensitiveService(BlogQueryStore blogs) {
        return new BlogSensitiveServiceImpl(blogs);
    }

    @Bean
    BlogIndexSourceQueries blogIndexSourceQueries(
        BlogQueryStore blogs, BlogIndexSourceState state) {
        return new BlogIndexSourceService(blogs, state);
    }

    @Bean
    BlogExportService blogExportService(BlogQueryStore blogs, BlogSearchGateway search) {
        return new BlogExportServiceImpl(blogs, search);
    }

    @Bean
    BlogAssetService blogAssetService(BlogAssetStorage storage, BlogQueryStore blogs) {
        return new BlogAssetServiceImpl(storage, blogs);
    }

    @Bean
    BlogCollaborationService blogCollaborationService(
        BlogQueryStore blogs,
        BlogAccessPolicy accessPolicy,
        BlogRuntimeStore runtimeStore,
        CollaborationTicketGateway tickets) {
        return new BlogCollaborationServiceImpl(blogs, accessPolicy, runtimeStore, tickets);
    }

    @Bean
    BlogStatisticsSync blogStatisticsSync(
        BlogQueryStore blogs, BlogStatisticsGateway statistics) {
        return new BlogStatisticsSyncService(blogs, statistics);
    }

    @Bean
    BlogRecycleBin blogRecycleBin(BlogRuntimeStore runtimeStore) {
        return new BlogRecycleBinServiceImpl(runtimeStore);
    }
}
