package wiki.chiu.micro.exhibit.adapter.out.http;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.api.BlogHttpService;
import wiki.chiu.micro.blog.api.vo.BlogPublicationContentRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogPublicationRpcVo;
import wiki.chiu.micro.cache.annotation.Cache;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;
import wiki.chiu.micro.exhibit.domain.PublishedContentCache;

@Component
public class BlogHttpServiceWrapper implements BlogCatalog {

    private final BlogHttpService blogHttpService;

    public BlogHttpServiceWrapper(BlogHttpService blogHttpService) {
        this.blogHttpService = blogHttpService;
    }

    @Override
    public List<Long> findIdsAfter(Long afterId, Integer limit) {
        return RemoteResult.requireSuccess(() -> blogHttpService.findIdsAfter(afterId, limit));
    }

    public BlogPublicationRpcVo publication(Long blogId) {
        return RemoteResult.requireSuccess(() -> blogHttpService.publication(blogId));
    }

    @Cache(namespace = PublishedContentCache.NAMESPACE, version = PublishedContentCache.VERSION, ttl = 5)
    public BlogPublicationContentRpcVo publicationContent(Long blogId, Long revisionId) {
        return RemoteResult.requireSuccess(() -> blogHttpService.publicationContent(blogId, revisionId));
    }

    public void setReadCount(Long id) {
        RemoteResult.requireSuccess(() -> blogHttpService.setReadCount(id));
    }
}
