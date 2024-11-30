package wiki.chiu.micro.search.provider;


import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.web.bind.annotation.*;
import wiki.chiu.micro.common.dto.AuthRpcDto;
import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.search.document.BlogDocument;
import wiki.chiu.micro.search.rpc.AuthHttpServiceWrapper;
import wiki.chiu.micro.search.rpc.BlogHttpServiceWrapper;
import wiki.chiu.micro.search.service.BlogSearchService;
import wiki.chiu.micro.search.vo.BlogSearchVo;
import org.springframework.validation.annotation.Validated;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@RestController
@Validated
@RequestMapping(value = "/inner")
public class SearchProvider {

    private static final Logger log = LoggerFactory.getLogger(SearchProvider.class);
    private final AuthHttpServiceWrapper authHttpServiceWrapper;

    private final BlogSearchService blogSearchService;

    public SearchProvider(AuthHttpServiceWrapper authHttpServiceWrapper, BlogSearchService blogSearchService, BlogHttpServiceWrapper blogHttpServiceWrapper, ElasticsearchTemplate elasticsearchTemplate) {
        this.authHttpServiceWrapper = authHttpServiceWrapper;
        this.blogSearchService = blogSearchService;
        this.blogHttpServiceWrapper = blogHttpServiceWrapper;
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @GetMapping("/blog/search")
    public Result<BlogSearchVo> searchAllBlogs(@RequestParam(defaultValue = "1") Integer currentPage,
                                               @RequestParam(defaultValue = "5") Integer size,
                                               @RequestParam @Size(max = 20) String keywords) {
        AuthRpcDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogSearchService.searchBlogs(keywords, currentPage, size, authDto.userId(), authDto.roles()));
    }

    @GetMapping("/blog/count")
    public Result<Long> searchCount(@RequestParam @Size(max = 20) String keywords) {
        AuthRpcDto authDto = authHttpServiceWrapper.getAuthentication();
        return Result.success(() -> blogSearchService.searchCount(keywords, authDto.userId(), authDto.roles()));
    }

    @PostMapping("/blog/read")
    public Result<Void> readCount(@RequestParam Long id) {
        return Result.success(() -> blogSearchService.addReadCount(id));
    }

    private final BlogHttpServiceWrapper blogHttpServiceWrapper;

    private final ElasticsearchTemplate elasticsearchTemplate;

    @GetMapping("/test")
    private void test() {
        for (long i = 1; i < 400; i++) {
            try {
                BlogEntityRpcDto blog = blogHttpServiceWrapper.findById(i);
                var blogDocument = BlogDocument.builder()
                        .id(blog.id())
                        .userId(blog.userId())
                        .title(blog.title())
                        .description(blog.description())
                        .content(blog.content())
                        .readCount(blog.readCount())
                        .status(blog.status())
                        .link(blog.link())
                        .created(ZonedDateTime.of(blog.created(), ZoneId.of("Asia/Shanghai")))
                        .updated(ZonedDateTime.of(blog.updated(), ZoneId.of("Asia/Shanghai")))
                        .build();

                elasticsearchTemplate.save(blogDocument);
            } catch (Exception e) {
                log.error("",e);
            }
        }
        elasticsearchTemplate.indexOps(IndexCoordinates.of("blog_index_v2")).delete();
    }
}
