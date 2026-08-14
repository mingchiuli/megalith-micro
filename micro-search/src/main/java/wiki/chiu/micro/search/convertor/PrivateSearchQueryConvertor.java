package wiki.chiu.micro.search.convertor;

import static wiki.chiu.micro.common.lang.FieldEnum.*;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionScoreMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.util.StringUtils;
import wiki.chiu.micro.common.lang.BlogStatusEnum;

public class PrivateSearchQueryConvertor {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
  private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

  private static final List<FieldValue> ALL_STATUS =
      Arrays.stream(BlogStatusEnum.values()).map(item -> FieldValue.of(item.getCode())).toList();

  public static NativeQuery countConvert(
      String keywords,
      Integer status,
      LocalDateTime createStart,
      LocalDateTime createEnd,
      Long userId,
      boolean allData) {
    var boolQuery = getSysBoolQuery(keywords, status, createStart, createEnd, userId, allData);

    return NativeQuery.builder().withQuery(query -> query.bool(boolQuery)).build();
  }

  public static NativeQuery searchConvert(
      String keywords,
      Integer status,
      LocalDateTime createStart,
      LocalDateTime createEnd,
      Long userId,
      boolean allData,
      Integer currentPage,
      Integer size) {

    boolean search = StringUtils.hasText(keywords);

    BoolQuery boolQuery =
        getSysBoolQuery(keywords, status, createStart, createEnd, userId, allData);

    var nativeQueryBuilder = NativeQuery.builder();
    if (search) {
      nativeQueryBuilder.withQuery(
          query ->
              query.functionScore(
                  functionScore ->
                      functionScore
                          .query(baseQry -> baseQry.bool(boolQuery))
                          .functions(
                              function ->
                                  function
                                      .filter(
                                          filter ->
                                              filter.matchPhrase(
                                                  matchPhrase ->
                                                      matchPhrase
                                                          .field(TITLE.getField())
                                                          .query(keywords)))
                                      .weight(2.0))
                          .functions(
                              function ->
                                  function
                                      .filter(
                                          filter ->
                                              filter.matchPhrase(
                                                  matchPhrase ->
                                                      matchPhrase
                                                          .field(DESCRIPTION.getField())
                                                          .query(keywords)))
                                      .weight(1.75))
                          .functions(
                              function ->
                                  function
                                      .filter(
                                          filter ->
                                              filter.matchPhrase(
                                                  matchPhrase ->
                                                      matchPhrase
                                                          .field(CONTENT.getField())
                                                          .query(keywords)))
                                      .weight(1.5))
                          .functions(
                              function ->
                                  function
                                      .filter(
                                          filter ->
                                              filter.match(
                                                  match ->
                                                      match
                                                          .field(TITLE.getField())
                                                          .query(keywords)))
                                      .weight(1.5))
                          .functions(
                              function ->
                                  function
                                      .filter(
                                          filter ->
                                              filter.match(
                                                  match ->
                                                      match
                                                          .field(DESCRIPTION.getField())
                                                          .query(keywords)))
                                      .weight(1.25))
                          .functions(
                              function ->
                                  function
                                      .filter(
                                          filter ->
                                              filter.match(
                                                  match ->
                                                      match
                                                          .field(CONTENT.getField())
                                                          .query(keywords)))
                                      .weight(1.0))
                          .scoreMode(FunctionScoreMode.Sum)
                          .boostMode(FunctionBoostMode.Multiply)));
    } else {
      nativeQueryBuilder.withQuery(query -> query.bool(boolQuery));
    }

    nativeQueryBuilder
        .withPageable(PageRequest.of(currentPage - 1, size))
        .withSort(
            search
                ? sort -> sort.score(score -> score.order(SortOrder.Desc))
                : sort ->
                    sort.field(field -> field.field(CREATED.getField()).order(SortOrder.Desc)))
        .build();

    return nativeQueryBuilder.build();
  }

  private static BoolQuery getSysBoolQuery(
      String keywords,
      Integer status,
      LocalDateTime createStart,
      LocalDateTime createEnd,
      Long userId,
      boolean allData) {
    BoolQuery.Builder boolQryBuilder = new BoolQuery.Builder();

    boolQryBuilder
        .mustNot(mustNot -> mustNot.term(term -> term.field("deleted").value(true)))
        .filter(
            filter ->
                filter.range(
                    range ->
                        range.term(
                            term ->
                                term.field(CREATED.getField())
                                    .gte(
                                        createStart != null
                                            ? ZonedDateTime.of(createStart, ZONE_ID)
                                                .format(FORMATTER)
                                            : null)
                                    .lte(
                                        createEnd != null
                                            ? ZonedDateTime.of(createEnd, ZONE_ID).format(FORMATTER)
                                            : null))))
        .filter(
            filter ->
                filter.terms(
                    terms ->
                        terms
                            .field(STATUS.getField())
                            .terms(
                                termsValue ->
                                    termsValue.value(
                                        status == null
                                            ? ALL_STATUS
                                            : Collections.singletonList(
                                                FieldValue.of(status.longValue()))))));

    if (StringUtils.hasText(keywords)) {
      boolQryBuilder
          .should(
              should ->
                  should.match(
                      match -> match.field(TITLE.getField()).fuzziness("auto").query(keywords)))
          .should(
              should ->
                  should.matchPhrase(
                      matchPhrase -> matchPhrase.field(TITLE.getField()).query(keywords)))
          .should(
              should ->
                  should.match(
                      match ->
                          match.field(DESCRIPTION.getField()).fuzziness("auto").query(keywords)))
          .should(
              should ->
                  should.matchPhrase(
                      matchPhrase -> matchPhrase.field(DESCRIPTION.getField()).query(keywords)))
          .should(
              should ->
                  should.match(
                      match -> match.field(CONTENT.getField()).fuzziness("auto").query(keywords)))
          .should(
              should ->
                  should.matchPhrase(
                      matchPhrase -> matchPhrase.field(CONTENT.getField()).query(keywords)))
          .minimumShouldMatch("1");
    }

    if (!allData) {
      boolQryBuilder.filter(
          filter -> filter.term(term -> term.field(USERID.getField()).value(userId)));
    }

    return boolQryBuilder.build();
  }
}
