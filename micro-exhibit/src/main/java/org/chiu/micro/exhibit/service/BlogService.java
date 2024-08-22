package org.chiu.micro.exhibit.service;

import org.chiu.micro.exhibit.vo.BlogDescriptionVo;
import org.chiu.micro.exhibit.vo.BlogExhibitVo;
import org.chiu.micro.exhibit.vo.BlogHotReadVo;
import org.chiu.micro.exhibit.vo.VisitStatisticsVo;
import org.chiu.micro.exhibit.page.PageAdapter;

import java.util.List;

/**
 * @author mingchiuli
 * @create 2022-11-27 2:12 pm
 */
public interface BlogService {

    PageAdapter<BlogDescriptionVo> findPage(Integer currentPage, Integer year);

    BlogExhibitVo getLockedBlog(Long blogId, String token);

    Boolean checkToken(Long blogId, String token);

    Integer getBlogStatus(List<String> roles, Long blogId, Long userId);

    List<Integer> searchYears();

    VisitStatisticsVo getVisitStatistics();

    List<BlogHotReadVo> getScoreBlogs();

    BlogExhibitVo getBlogDetail(List<String> roles, Long id, Long userId);

    Long getCountByYear(Integer year);

    Long count();

    List<Integer> getYears();

    List<Long> findIds(Integer pageNo, Integer pageSize);

}
