package wiki.chiu.micro.search.convertor;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.lang.BlogStatusEnum;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static wiki.chiu.micro.common.lang.FieldEnum.*;

public class PrivateSearchQueryConvertor {


    private static final DateTimeFormatter FORMATTER =  DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    private static final List<FieldValue> ALL_STATUS = Arrays.stream(BlogStatusEnum.values())
            .map(item -> FieldValue.of(item.getCode()))
            .toList();

    public static NativeQuery countConvert(String keywords, Integer status, LocalDateTime createStart, LocalDateTime createEnd, Long userId, List<String> roles, String highestRole) {
        var boolQuery = getSysBoolQuery(keywords, status, createStart, createEnd, userId, roles, highestRole);

        return NativeQuery.builder()
                .withQuery(query ->
                        query.bool(boolQuery))
                .build();
    }


    public static NativeQuery searchConvert(String keywords, Integer status, LocalDateTime createStart, LocalDateTime createEnd, Long userId, List<String> roles, String highestRole, Integer currentPage, Integer size) {

        boolean search = StringUtils.hasText(keywords);

        BoolQuery boolQuery = getSysBoolQuery(keywords, status, createStart, createEnd, userId, roles, highestRole);

        var nativeQueryBuilder = NativeQuery.builder();
        if (search) {
            nativeQueryBuilder
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
                                    .boostMode(FunctionBoostMode.Multiply)));
        } else {
            nativeQueryBuilder.withQuery(query -> query
                    .bool(boolQuery));
        }

        nativeQueryBuilder
                .withPageable(PageRequest.of(currentPage - 1, size))
                .withSort(search
                        ? sort -> sort
                        .score(score -> score
                                .order(SortOrder.Desc))
                        : sort -> sort
                        .field(field -> field
                                .field(CREATED.getField())
                                .order(SortOrder.Desc)))
                .build();

        return nativeQueryBuilder.build();
    }


    private static BoolQuery getSysBoolQuery(String keywords, Integer status, LocalDateTime createStart, LocalDateTime createEnd, Long userId, List<String> roles, String highestRole) {
        BoolQuery.Builder boolQryBuilder = new BoolQuery.Builder();

        boolQryBuilder
                .filter(filter -> filter
                        .range(range -> range
                                .field(CREATED.getField())
                                .from(createStart != null
                                        ? ZonedDateTime.of(createStart, ZONE_ID).format(FORMATTER)
                                        : null)
                                .to(createEnd != null
                                        ? ZonedDateTime.of(createEnd, ZONE_ID).format(FORMATTER)
                                        : null)))
                .filter(filter -> filter
                        .terms(terms -> terms
                                .field(STATUS.getField())
                                .terms(termsValue -> termsValue
                                        .value(status == null
                                                ? ALL_STATUS
                                                : Collections.singletonList(FieldValue.of(status.longValue()))))));

        if (StringUtils.hasText(keywords)) {
            boolQryBuilder
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
        }

        if (!roles.contains(highestRole)) {
            var boolQry = BoolQuery.of(bool -> bool
                    .should(should -> should
                            .term(term -> term
                                    .field(USERID.getField())
                                    .value(userId)))
                    .should(should -> should
                            .term(term -> term
                                    .field(STATUS.getField())
                                    .value(BlogStatusEnum.NORMAL.getCode())))
                    .minimumShouldMatch("1"));
            boolQryBuilder.filter(filter -> filter.bool(boolQry));
        }

        return boolQryBuilder.build();
    }
}
