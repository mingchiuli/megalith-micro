package wiki.chiu.micro.exhibit.application.port.in;

import java.util.List;

import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.BlogHotRead;
import wiki.chiu.micro.exhibit.application.model.RecommendationFeed;
import wiki.chiu.micro.exhibit.application.model.VisitStatistics;

/**
 * @author mingchiuli
 * @create 2022-11-27 2:12 pm
 */
public interface BlogService {

    BlogExhibit getLockedBlog(Long blogId, String token);

    VisitStatistics getVisitStatistics();

    List<BlogHotRead> getScoreBlogs();

    RecommendationFeed findRecommendationFeed(String cursor);

    BlogExhibit getBlogDetail(List<DataPermissionEnum> dataPermissions, Long id, Long userId);
}
