package wiki.chiu.micro.exhibit.application.port.in;

import java.util.List;
import wiki.chiu.micro.common.lang.DataPermissionEnum;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.vo.BlogDescriptionVo;
import wiki.chiu.micro.exhibit.vo.BlogExhibitVo;
import wiki.chiu.micro.exhibit.vo.BlogHotReadVo;
import wiki.chiu.micro.exhibit.vo.VisitStatisticsVo;

/**
 * @author mingchiuli
 * @create 2022-11-27 2:12 pm
 */
public interface BlogService {

  PageAdapter<BlogDescriptionVo> findPage(Integer currentPage);

  BlogExhibitVo getLockedBlog(Long blogId, String token);

  VisitStatisticsVo getVisitStatistics();

  List<BlogHotReadVo> getScoreBlogs();

  BlogExhibitVo getBlogDetail(List<DataPermissionEnum> dataPermissions, Long id, Long userId);
}
