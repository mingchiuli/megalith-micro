package org.chiu.micro.search.service.impl;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;

import co.elastic.clients.json.JsonData;

import org.chiu.micro.search.lang.StatusEnum;
import org.chiu.micro.search.convertor.BlogEntityVoConvertor;
import org.chiu.micro.search.vo.BlogEntityVo;
import org.chiu.micro.search.page.PageAdapter;
import org.chiu.micro.search.convertor.BlogDocumentVoConvertor;
import org.chiu.micro.search.document.BlogDocument;
import org.chiu.micro.search.service.BlogSearchService;
import org.chiu.micro.search.utils.ESHighlightBuilderUtils;
import org.chiu.micro.search.vo.BlogDocumentVo;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

import static org.chiu.micro.search.lang.FieldEnum.*;

/**
 * @author mingchiuli
 * @create 2022-11-30 9:00 pm
 */
@Service
@RequiredArgsConstructor
public class BlogSearchServiceImpl implements BlogSearchService {

    private final ElasticsearchTemplate elasticsearchTemplate;

    @Value("${blog.blog-page-size}")
    private int blogPageSize;

    @Value("${blog.highest-role}")
    private String highestRole;

    @Override
    public PageAdapter<BlogDocumentVo> selectBlogsByES(Integer currentPage, String keywords, Boolean allInfo, String year) {

        var matchQuery = NativeQuery.builder()
                .withQuery(query -> query
                        .functionScore(functionScore -> functionScore
                                .query(baseQuery -> baseQuery
                                        //做高亮必須在query里搜高亮字段
                                        //不做高亮可以不写，但是也基于评分策略
                                        .bool(boolQry -> boolQry
                                                .should(should -> should
                                                        .match(match -> match
                                                                .field(TITLE.getField())
                                                                .fuzziness("auto")
                                                                .query(keywords)))
                                                .should(should -> should
                                                        .matchPhrase(matchPhrase -> matchPhrase
                                                                .field(TITLE.getField())
                                                                .query(keywords)))
                                                .should(should -> should
                                                        .match(match -> match
                                                                .field(DESCRIPTION.getField())
                                                                .fuzziness("auto")
                                                                .query(keywords)))
                                                .should(should -> should
                                                        .matchPhrase(matchPhrase -> matchPhrase
                                                                .field(DESCRIPTION.getField())
                                                                .query(keywords)))
                                                .should(should -> should
                                                        .match(match -> match
                                                                .field(CONTENT.getField())
                                                                .fuzziness("auto")
                                                                .query(keywords))).
                                                should(should -> should
                                                        .matchPhrase(matchPhrase -> matchPhrase
                                                                .field(CONTENT.getField())
                                                                .query(keywords)))
                                                .minimumShouldMatch("1")
                                                .filter(filter -> filter
                                                        .range(range -> range
                                                                .field(CREATED.getField())
                                                                .from(StringUtils.hasLength(year) ? year + "-01-01T00:00:00.000+08:00" : null)
                                                                .to(StringUtils.hasLength(year) ? year + "-12-31T23:59:59.999+08:00" : null)))
                                                .filter(filter -> filter
                                                        .term(termQry -> termQry
                                                                .field(STATUS.getField())
                                                                .value(StatusEnum.NORMAL.getCode())))))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .match(match -> match
                                                        .field(TITLE.getField())
                                                        .query(keywords)))
                                        .weight(1.0))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .match(match -> match
                                                        .field(DESCRIPTION.getField())
                                                        .query(keywords)))
                                        .weight(1.25))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .match(match -> match
                                                        .field(CONTENT.getField())
                                                        .query(keywords)))
                                        .weight(1.5))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .matchPhrase(matchPhrase -> matchPhrase
                                                        .field(TITLE.getField())
                                                        .query(keywords)))
                                        .weight(1.5))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .matchPhrase(matchPhrase -> matchPhrase
                                                        .field(DESCRIPTION.getField())
                                                        .query(keywords)))
                                        .weight(1.75))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .matchPhrase(matchPhrase -> matchPhrase
                                                        .field(CONTENT.getField())
                                                        .query(keywords)))
                                        .weight(2.0))
                                .functions(function -> function
                                        .gauss(gauss -> gauss
                                                .field(UPDATED.getField())
                                                .placement(placement -> placement
                                                        .origin(JsonData.of("now/d"))
                                                        .scale(JsonData.of("1095d"))
                                                        .offset(JsonData.of("90d"))
                                                        .decay(0.5))))
                                .scoreMode(FunctionScoreMode.Sum)
                                .boostMode(FunctionBoostMode.Multiply)))
                .withSort(sort -> sort
                        .score(score -> score
                                .order(SortOrder.Desc)))
                .withPageable(Objects.equals(currentPage, -1) ?
                        PageRequest.of(0, 10) :
                        PageRequest.of(currentPage - 1, blogPageSize))
                .withHighlightQuery(Boolean.TRUE.equals(allInfo) ?
                        ESHighlightBuilderUtils.blogHighlightQueryOrigin :
                        ESHighlightBuilderUtils.blogHighlightQuerySimple)
                .build();
        SearchHits<BlogDocument> search = elasticsearchTemplate.search(matchQuery, BlogDocument.class);

        return BlogDocumentVoConvertor.convert(search, blogPageSize, currentPage);
    }

    @Override
    public PageAdapter<BlogEntityVo> searchAllBlogs(String keywords, Integer currentPage, Integer size, Long userId, List<String> roles) {

        var boolQryBuilder = new BoolQuery.Builder();
        var builder = boolQryBuilder
                .should(should -> should
                        .match(match -> match
                                .field(TITLE.getField())
                                .fuzziness("auto")
                                .query(keywords)))
                .should(should -> should
                        .matchPhrase(matchPhrase -> matchPhrase
                                .field(TITLE.getField())
                                .query(keywords)))
                .should(should -> should
                        .match(match -> match
                                .field(DESCRIPTION.getField())
                                .fuzziness("auto")
                                .query(keywords)))
                .should(should -> should
                        .matchPhrase(matchPhrase -> matchPhrase
                                .field(DESCRIPTION.getField())
                                .query(keywords)))
                .should(should -> should
                        .match(match -> match
                                .field(CONTENT.getField())
                                .fuzziness("auto")
                                .query(keywords)))
                .should(should -> should
                        .matchPhrase(matchPhrase -> matchPhrase
                                .field(CONTENT.getField())
                                .query(keywords)))
                .minimumShouldMatch("1");

        if (!roles.contains(highestRole)) {
            var filterQry = Query.of(filter -> filter
                    .term(term -> term
                            .field(USERID.getField())
                            .value(userId)));
            builder.filter(filterQry);
        }

        BoolQuery boolQuery = builder.build();

        var nativeQuery = NativeQuery.builder()
                .withQuery(query -> query
                        .functionScore(functionScore -> functionScore
                                .query(baseQry -> baseQry
                                        .bool(boolQuery))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .matchPhrase(matchPhrase -> matchPhrase
                                                        .field(TITLE.getField())
                                                        .query(keywords)))
                                        .weight(2.0))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .matchPhrase(matchPhrase -> matchPhrase
                                                        .field(DESCRIPTION.getField())
                                                        .query(keywords)))
                                        .weight(1.75))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .matchPhrase(matchPhrase -> matchPhrase
                                                        .field(CONTENT.getField())
                                                        .query(keywords)))
                                        .weight(1.5))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .match(match -> match
                                                        .field(TITLE.getField())
                                                        .query(keywords)))
                                        .weight(1.5))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .match(match -> match
                                                        .field(DESCRIPTION.getField())
                                                        .query(keywords)))
                                        .weight(1.25))
                                .functions(function -> function
                                        .filter(filter -> filter
                                                .match(match -> match
                                                        .field(CONTENT.getField())
                                                        .query(keywords)))
                                        .weight(1.0))
                                .scoreMode(FunctionScoreMode.Sum)
                                .boostMode(FunctionBoostMode.Multiply)))
                .withPageable(PageRequest.of(currentPage - 1, size))
                .withSort(sort -> sort
                        .score(score -> score
                                .order(SortOrder.Desc)))
                .build();

        SearchHits<BlogDocument> search = elasticsearchTemplate.search(nativeQuery, BlogDocument.class);
        return BlogEntityVoConvertor.convert(search, currentPage, size, userId);
    }

}
