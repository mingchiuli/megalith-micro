package org.chiu.micro.search.service;


import org.chiu.micro.search.vo.BlogEntityVo;

import java.util.List;

import org.chiu.micro.search.page.PageAdapter;
import org.chiu.micro.search.vo.BlogDocumentVo;

/**
 * @author mingchiuli
 * @create 2022-11-30 8:52 pm
 */
public interface BlogSearchService {

    PageAdapter<BlogDocumentVo> selectBlogsByES(Integer currentPage, String keywords, Boolean allInfo, String year);

    PageAdapter<BlogEntityVo> searchAllBlogs(String keywords, Integer currentPage, Integer size, Long userId, List<String> roles);
}
