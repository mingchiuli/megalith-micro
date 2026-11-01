package wiki.chiu.micro.search.adapter.out.elasticsearch;

import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;

final class BlogSearchQueryFactory {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    private BlogSearchQueryFactory() {
    }

    static NativeQuery recommendation(
        Instant snapshotTime, List<Object> searchAfter, int pageSize) {
        NativeQuery query = NativeQuery.builder()
            .withQuery(
                value ->
                    value.functionScore(
                        score ->
                            score.query(
                                    base ->
                                        base.bool(
                                            bool ->
                                                bool.filter(
                                                        filter ->
                                                            filter.term(
                                                                term ->
                                                                    term.field("status")
                                                                        .value(
                                                                            BlogStatusEnum.NORMAL
                                                                                .getCode())))
                                                    .mustNot(
                                                        not ->
                                                            not.term(
                                                                term ->
                                                                    term.field("deleted")
                                                                        .value(true)))))
                                .functions(
                                    fn ->
                                        fn.scriptScore(
                                            script ->
                                                script.script(
                                                    source ->
                                                        source.source(
                                                            scriptSource ->
                                                                scriptSource.scriptString(
                                                                    """
                                                                    if (doc['readCount'].size() == 0) {
                                                                        return 0;
                                                                    }
                                                                    return Math.log(doc['readCount'].value + 1);
                                                                    """)))))
                                .functions(
                                    fn ->
                                        fn.gauss(
                                            gauss ->
                                                gauss.date(
                                                    date ->
                                                        date.field("publishedAt")
                                                            .placement(
                                                                placement ->
                                                                    placement
                                                                        .origin(
                                                                            snapshotTime
                                                                                .toString())
                                                                        .scale(
                                                                            Time.of(
                                                                                time ->
                                                                                    time.time(
                                                                                        "180d")))
                                                                        .offset(
                                                                            Time.of(
                                                                                time ->
                                                                                    time.time(
                                                                                        "30d")))
                                                                        .decay(0.5)))))
                                .boostMode(FunctionBoostMode.Sum)))
            .withSort(sort -> sort.score(score -> score.order(SortOrder.Desc)))
            .withSort(sort -> sort.field(field -> field.field("id").order(SortOrder.Asc)))
            .withPageable(PageRequest.of(0, pageSize))
            .build();
        query.setSearchAfter(searchAfter);
        return query;
    }

    static NativeQuery admin(PrivateBlogSearchQuery query) {
        boolean draftOnly =
            query.draftOnly()
                || Objects.equals(query.status(), BlogStatusEnum.DRAFT.getCode());
        return NativeQuery.builder()
            .withQuery(
                value ->
                    value.bool(
                        bool -> {
                            if (!query.allData()) {
                                bool.filter(
                                    filter ->
                                        filter.term(
                                            term ->
                                                term.field("userId").value(query.userId())));
                            }
                            if (query.createStart() != null || query.createEnd() != null) {
                                String start =
                                    query.createStart() == null
                                        ? null
                                        : ZonedDateTime.of(query.createStart(), ZONE_ID)
                                            .format(
                                                DateTimeFormatter.ofPattern(
                                                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
                                String end =
                                    query.createEnd() == null
                                        ? null
                                        : ZonedDateTime.of(query.createEnd(), ZONE_ID)
                                            .format(
                                                DateTimeFormatter.ofPattern(
                                                    "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"));
                                bool.filter(
                                    filter ->
                                        filter.range(
                                            range ->
                                                range.term(
                                                    term ->
                                                        term.field("created")
                                                            .gte(start)
                                                            .lte(end))));
                            }
                            bool.mustNot(
                                not ->
                                    not.term(
                                        term -> term.field("deleted").value(true)));
                            if (draftOnly) {
                                bool.filter(
                                    filter ->
                                        filter.term(
                                            term -> term.field("hasDraft").value(true)));
                            } else {
                                bool.filter(
                                    filter ->
                                        filter.exists(
                                            exists -> exists.field("publishedRevisionId")));
                                if (query.status() != null) {
                                    bool.filter(
                                        filter ->
                                            filter.term(
                                                term ->
                                                    term.field("status")
                                                        .value(query.status())));
                                }
                            }
                            if (query.keywords() != null && !query.keywords().isBlank()) {
                                String keyword = query.keywords();
                                if (draftOnly) {
                                    bool.should(
                                            item ->
                                                item.match(
                                                    match ->
                                                        match.field("draftTitle").query(keyword)))
                                        .should(
                                            item ->
                                                item.match(
                                                    match ->
                                                        match.field("draftDescription")
                                                            .query(keyword)))
                                        .should(
                                            item ->
                                                item.match(
                                                    match ->
                                                        match.field("draftContent")
                                                            .query(keyword)));
                                } else {
                                    bool.should(
                                            item ->
                                                item.match(
                                                    match ->
                                                        match.field("publishedTitle").query(keyword)))
                                        .should(
                                            item ->
                                                item.match(
                                                    match ->
                                                        match.field("publishedDescription")
                                                            .query(keyword)))
                                        .should(
                                            item ->
                                                item.match(
                                                    match ->
                                                        match.field("publishedContent")
                                                            .query(keyword)));
                                }
                                bool.minimumShouldMatch("1");
                            }
                            return bool;
                        }))
            .withPageable(PageRequest.of(query.page() - 1, query.pageSize()))
            .withSort(
                sort ->
                    sort.field(
                        field -> field.field("created").order(SortOrder.Desc)))
            .withSort(
                sort ->
                    sort.field(field -> field.field("id").order(SortOrder.Asc)))
            .build();
    }

    static NativeQuery revision(RevisionSearchQuery query) {
        return NativeQuery.builder()
            .withQuery(
                value ->
                    value.bool(
                        bool -> {
                            bool.filter(
                                filter ->
                                    filter.term(
                                        term -> term.field("blogId").value(query.blogId())));
                            if (!query.allData()) {
                                bool.filter(
                                    filter ->
                                        filter.term(
                                            term ->
                                                term.field("createdBy").value(query.userId())));
                            }
                            if (query.keywords() != null && !query.keywords().isBlank()) {
                                String keyword = query.keywords();
                                bool.should(
                                        item ->
                                            item.match(
                                                match -> match.field("title").query(keyword)))
                                    .should(
                                        item ->
                                            item.match(
                                                match ->
                                                    match.field("description").query(keyword)))
                                    .should(
                                        item ->
                                            item.match(
                                                match -> match.field("content").query(keyword)))
                                    .minimumShouldMatch("1");
                            }
                            return bool;
                        }))
            .withPageable(PageRequest.of(query.page() - 1, query.pageSize()))
            .withSort(
                sort ->
                    sort.field(
                        field -> field.field("versionNo").order(SortOrder.Desc)))
            .build();
    }
}
