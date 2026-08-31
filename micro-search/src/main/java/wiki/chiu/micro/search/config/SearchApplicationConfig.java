package wiki.chiu.micro.search.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

import wiki.chiu.micro.search.adapter.out.elasticsearch.ElasticsearchBlogAdapter;
import wiki.chiu.micro.search.application.port.in.ApplyBlogIndexChangeUseCase;
import wiki.chiu.micro.search.application.port.in.SearchBlogsUseCase;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;
import wiki.chiu.micro.search.application.service.BlogIndexApplicationService;
import wiki.chiu.micro.search.application.service.SearchApplicationService;

@Configuration(proxyBeanMethods = false)
public class SearchApplicationConfig {

    @Bean
    ElasticsearchBlogAdapter elasticsearchBlogAdapter(
        ElasticsearchTemplate elasticsearchTemplate,
        @Value("${megalith.blog.blog-page-size}") int publicPageSize) {
        return new ElasticsearchBlogAdapter(elasticsearchTemplate, publicPageSize);
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
