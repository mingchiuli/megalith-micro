package wiki.chiu.micro.search.convertor;

import co.elastic.clients.elasticsearch._types.ScriptLanguage;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import co.elastic.clients.json.JsonData;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightParameters;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.lang.StatusEnum;

import java.util.List;

import static wiki.chiu.micro.common.lang.FieldEnum.*;

public class PublicSearchQueryConvertor {

    private static final String RED = "<b style='color:red'>";

    private static final String BABEL = "</b>";

    public static NativeQuery searchConvert(String keywords, String year, Integer currentPage, Integer blogPageSize, Boolean allInfo) {
        return NativeQuery.builder()
                .withQuery(query -> query
                        .functionScore(functionScore -> functionScore
                                .query(baseQuery -> baseQuery
                                        // 做高亮必須在query里搜高亮字段
                                        // 不做高亮可以不写，但是也基于评分策略
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
                                                                .query(keywords)))
                                                .should(should -> should
                                                        .matchPhrase(matchPhrase -> matchPhrase
                                                                .field(CONTENT.getField())
                                                                .query(keywords)))
                                                .minimumShouldMatch("1")
                                                .filter(filter -> filter
                                                        .range(range -> range
                                                                .field(CREATED.getField())
                                                                .from(StringUtils.hasLength(year)
                                                                        ? year + "-01-01T00:00:00.000+08:00"
                                                                        : null)
                                                                .to(StringUtils.hasLength(year)
                                                                        ? year + "-12-31T23:59:59.999+08:00"
                                                                        : null)))
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
                                .functions(function -> function
                                        .scriptScore(scriptScore -> scriptScore
                                                .script(script -> script
                                                        .inline(inline -> inline
                                                                .source("def c = doc['readCount'].value;return Math.log(c + 3);")
                                                                .lang(ScriptLanguage.Painless)))))
                                .scoreMode(FunctionScoreMode.Sum)
                                .boostMode(FunctionBoostMode.Multiply)))
                .withSort(sort -> sort
                        .score(score -> score
                                .order(SortOrder.Desc)))
                .withPageable(PageRequest.of(currentPage - 1, blogPageSize))
                .withHighlightQuery(Boolean.TRUE.equals(allInfo)
                        ? new HighlightQuery(
                                new Highlight(
                                        new HighlightParameters
                                                .HighlightParametersBuilder()
                                                .withPreTags(RED)
                                                .withPostTags(BABEL)
                                                .build(),
                                        List.of(new HighlightField(TITLE.getField()),
                                                new HighlightField(DESCRIPTION.getField()),
                                                new HighlightField(CONTENT.getField()))),
                        null)
                        : new HighlightQuery(
                                new Highlight(
                                        new HighlightParameters
                                                .HighlightParametersBuilder()
                                                .withPreTags(RED)
                                                .withPostTags(BABEL)
                                                //为0则全部内容都显示
                                                .withNumberOfFragments(1)
                                                .withFragmentSize(5)
                                                .build(),
                                        List.of(new HighlightField(TITLE.getField()),
                                                new HighlightField(DESCRIPTION.getField()),
                                                new HighlightField(CONTENT.getField()))),
                        null))
                .build();
    }
}
