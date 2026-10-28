package wiki.chiu.micro.blog.application.port.in;

import java.util.List;

public interface BlogQueryService {

    List<Long> findIdsAfter(Long afterId, Integer limit);

    void incrementViews(Long blogId);
}
