package wiki.chiu.micro.exhibit.adapter.out.http;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.api.BlogHttpService;
import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.blog.api.vo.BlogSensitiveContentRpcVo;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.exhibit.application.port.out.BlogCatalog;

/**
 * BlogHttpServiceWrapper
 */
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

    public BlogEntityRpcVo findById(Long blogId) {
        return RemoteResult.requireSuccess(() -> blogHttpService.findById(blogId));
    }

    @Override
    public List<BlogEntityRpcVo> findAllById(List<Long> ids) {
        return RemoteResult.requireSuccess(() -> blogHttpService.findAllById(ids));
    }

    public Long count() {
        return RemoteResult.requireSuccess(blogHttpService::count);
    }

    public void setReadCount(Long id) {
        RemoteResult.requireSuccess(() -> blogHttpService.setReadCount(id));
    }

    public PageAdapter<BlogEntityRpcVo> findPage(Integer pageNo, Integer pageSize) {
        return RemoteResult.requireSuccess(() -> blogHttpService.findPage(pageNo, pageSize));
    }

    public BlogSensitiveContentRpcVo findSensitiveByBlogId(Long blogId) {
        return RemoteResult.requireSuccess(() -> blogHttpService.findSensitiveByBlogId(blogId));
    }
}
