package wiki.chiu.micro.blog.application.port.in;

import java.util.List;

import wiki.chiu.micro.blog.api.vo.BlogEntityRpcVo;
import wiki.chiu.micro.common.page.PageAdapter;

public interface BlogQueryService {

    List<Long> findIdsAfter(Long afterId, Integer limit);

    BlogEntityRpcVo findById(Long blogId);

    List<BlogEntityRpcVo> findAllById(List<Long> ids);

    long count();

    void incrementViews(Long blogId);

    PageAdapter<BlogEntityRpcVo> findPage(Integer pageNo, Integer pageSize);

}
