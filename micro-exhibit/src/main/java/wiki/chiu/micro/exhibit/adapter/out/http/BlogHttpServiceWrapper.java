package wiki.chiu.micro.exhibit.adapter.out.http;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.api.BlogHttpService;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;

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

    public void setReadCount(Long id) {
        RemoteResult.requireSuccess(() -> blogHttpService.setReadCount(id));
    }
}
