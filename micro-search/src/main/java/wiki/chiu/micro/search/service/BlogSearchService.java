package wiki.chiu.micro.search.service;



import java.util.List;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.vo.BlogDocumentVo;
import wiki.chiu.micro.search.vo.BlogSearchVo;

/**
 * @author mingchiuli
 * @create 2022-11-30 8:52 pm
 */
public interface BlogSearchService {

    PageAdapter<BlogDocumentVo> selectBlogsByES(Integer currentPage, String keywords, Boolean allInfo, String year);

    BlogSearchVo searchBlogs(String keywords, Integer currentPage, Integer size, Long userId, List<String> roles);

    Long searchCount(String keywords, Long userId, List<String> roles);

    void addReadCount(Long id);

}
