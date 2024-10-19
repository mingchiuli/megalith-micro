package org.chiu.micro.search.service;



import java.util.List;

import org.chiu.micro.common.page.PageAdapter;
import org.chiu.micro.search.vo.BlogDocumentVo;
import org.chiu.micro.search.vo.BlogSearchVo;

/**
 * @author mingchiuli
 * @create 2022-11-30 8:52 pm
 */
public interface BlogSearchService {

    PageAdapter<BlogDocumentVo> selectBlogsByES(Integer currentPage, String keywords, Boolean allInfo, String year);

    BlogSearchVo searchBlogs(String keywords, Integer currentPage, Integer size, Long userId, List<String> roles);
}
