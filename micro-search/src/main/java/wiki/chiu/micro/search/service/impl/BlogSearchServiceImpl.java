package wiki.chiu.micro.search.service.impl;

import co.elastic.clients.elasticsearch._types.ScriptLanguage;

import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.ScriptType;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.search.convertor.PrivateSearchQueryConvertor;
import wiki.chiu.micro.search.convertor.PublicSearchQueryConvertor;
import wiki.chiu.micro.search.convertor.BlogDocumentVoConvertor;
import wiki.chiu.micro.search.document.BlogDocument;
import wiki.chiu.micro.search.lang.IndexConst;
import wiki.chiu.micro.search.service.BlogSearchService;
import wiki.chiu.micro.search.vo.BlogDocumentVo;
import wiki.chiu.micro.search.vo.BlogSearchVo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;


/**
 * @author mingchiuli
 * @create 2022-11-30 9:00 pm
 */
@Service
public class BlogSearchServiceImpl implements BlogSearchService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    @Value("${megalith.blog.blog-page-size}")
    private int blogPageSize;

    @Value("${megalith.blog.highest-role}")
    private String highestRole;


    public BlogSearchServiceImpl(ElasticsearchTemplate elasticsearchTemplate) {
        this.elasticsearchTemplate = elasticsearchTemplate;
    }

    @Override
    public PageAdapter<BlogDocumentVo> selectBlogsByES(Integer currentPage, String keywords, Boolean allInfo, String year) {
        NativeQuery matchQuery = PublicSearchQueryConvertor.searchConvert(keywords, year, currentPage, blogPageSize, allInfo);
        SearchHits<BlogDocument> search = elasticsearchTemplate.search(matchQuery, BlogDocument.class);
        return BlogDocumentVoConvertor.convert(search, blogPageSize, currentPage);
    }

    @Override
    public BlogSearchVo searchBlogs(BlogSysSearchReq req, Long userId, List<String> roles) {

        Integer size = req.pageSize();
        Integer currentPage = req.page();

        NativeQuery matchQuery = PrivateSearchQueryConvertor.searchConvert(req.keywords(), req.status(), req.createStart(), req.createEnd(), userId, roles, highestRole, currentPage, size);
        SearchHits<BlogDocument> searchResp = elasticsearchTemplate.search(matchQuery, BlogDocument.class);

        List<Long> ids = searchResp.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(BlogDocument::getId)
                .toList();

        return BlogSearchVo.builder()
                .ids(ids)
                .currentPage(currentPage)
                .size(size)
                .total(searchResp.getTotalHits())
                .build();

    }

    @Override
    public Long searchCount(BlogSysCountSearchReq req, Long userId, List<String> roles) {
        NativeQuery countQuery = PrivateSearchQueryConvertor.countConvert(req.keywords(), req.status(), req.createStart(), req.createEnd(), userId, roles, highestRole);
        return elasticsearchTemplate.count(countQuery, BlogDocument.class);
    }

    @Override
    public void addReadCount(Long id) {
        var updateQuery = UpdateQuery.builder(id.toString())
                .withScript("ctx._source.readCount += 1;")
                .withLang(ScriptLanguage.Painless.jsonValue())
                .withScriptType(ScriptType.INLINE)
                .build();
        elasticsearchTemplate.update(updateQuery, IndexCoordinates.of(IndexConst.indexName));
    }

}
