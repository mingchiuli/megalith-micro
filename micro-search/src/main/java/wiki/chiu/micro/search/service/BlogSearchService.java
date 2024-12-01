package wiki.chiu.micro.search.service;

import java.util.List;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.search.vo.BlogDocumentVo;
import wiki.chiu.micro.search.vo.BlogSearchVo;

/**
 * @author mingchiuli
 * @create 2022-11-30 8:52 pm
 */
public interface BlogSearchService {

    PageAdapter<BlogDocumentVo> selectBlogsByES(Integer currentPage, String keywords, Boolean allInfo, String year);

    BlogSearchVo searchBlogs(BlogSysSearchReq req, Long userId, List<String> roles);

    Long searchCount(BlogSysCountSearchReq req, Long userId, List<String> roles);

    void addReadCount(Long id);

}
