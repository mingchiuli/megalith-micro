package wiki.chiu.micro.blog.application.port.in;

import java.util.List;

import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.page.PageAdapter;

public interface BlogQueryService {

    List<Long> findIdsAfter(Long afterId, Integer limit);

    BlogSnapshot findById(Long blogId);

    List<BlogSnapshot> findAllById(List<Long> ids);

    long count();

    void incrementViews(Long blogId);

    PageAdapter<BlogSnapshot> findPage(Integer pageNo, Integer pageSize);
}
