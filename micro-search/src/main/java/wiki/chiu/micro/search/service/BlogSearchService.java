package wiki.chiu.micro.search.service;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.vo.BlogSearchRpcVo;
import wiki.chiu.micro.search.vo.BlogDocumentVo;

/**
 * @author mingchiuli
 * @create 2022-11-30 8:52 pm
 */
public interface BlogSearchService {

    PageAdapter<BlogDocumentVo> selectBlogsByES(Integer currentPage, String keywords, Boolean allInfo, String year);

    BlogSearchRpcVo searchBlogs(BlogSysSearchReq req);

    Long searchCount(BlogSysCountSearchReq req);

    void addReadCount(Long id);

}
