package wiki.chiu.micro.search.adapter.out.elasticsearch;

import co.elastic.clients.elasticsearch._types.ScriptLanguage;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.query_dsl.FunctionBoostMode;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;

import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogReadCount;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.BlogSearchResult;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogRead;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.RecommendationResult;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;
import wiki.chiu.micro.search.application.port.out.BlogIndexWriter;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;
import wiki.chiu.micro.search.domain.BlogIndexEntry;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogStatusEnum;

public final class ElasticsearchBlogAdapter implements BlogSearchIndex, BlogIndexWriter {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    private final ElasticsearchTemplate elasticsearchTemplate;
    private final int publicPageSize;
    private final String indexName;
    private final String adminIndexName;
    private final String revisionIndexName;
    private final String contentScript;
    private final String readCountScript;

    public ElasticsearchBlogAdapter(
        ElasticsearchTemplate elasticsearchTemplate, int publicPageSize) {
        this(
            elasticsearchTemplate,
            publicPageSize,
            IndexConst.indexName,
            IndexConst.adminIndexName,
            IndexConst.revisionIndexName);
    }

    public ElasticsearchBlogAdapter(
        ElasticsearchTemplate elasticsearchTemplate, int publicPageSize, String indexName) {
        this(
            elasticsearchTemplate,
            publicPageSize,
            indexName,
            IndexConst.adminIndexName,
            IndexConst.revisionIndexName);
    }

    public ElasticsearchBlogAdapter(
        ElasticsearchTemplate elasticsearchTemplate,
        int publicPageSize,
        String indexName,
        String adminIndexName,
        String revisionIndexName) {
        this.elasticsearchTemplate = elasticsearchTemplate;
        this.publicPageSize = publicPageSize;
        this.indexName = indexName;
        this.adminIndexName = adminIndexName;
        this.revisionIndexName = revisionIndexName;
        this.contentScript = script("blog-content-update.painless");
        this.readCountScript = script("blog-read-count-update.painless");
    }

    @Override
    public Page<BlogSearchHit> searchPublic(PublicBlogSearchQuery query) {
        NativeQuery searchQuery =
            PublicSearchQueryConvertor.searchConvert(
                query.keywords(), query.page(), publicPageSize, query.allInfo());
        SearchHits<@NonNull BlogDocument> hits =
            elasticsearchTemplate.search(searchQuery, BlogDocument.class, IndexCoordinates.of(indexName));
        return toPage(hits, query.page(), publicPageSize);
    }

    @Override
    public Page<PublicBlogRead> publicPage(int page, int pageSize) {
        NativeQuery query =
            NativeQuery.builder()
                .withQuery(
                    value ->
                        value.bool(
                            bool ->
                                bool.filter(
                                        filter ->
                                            filter.terms(
                                                terms ->
                                                    terms.field("status")
                                                        .terms(
                                                            termsValue ->
                                                                termsValue.value(
                                                                    List.of(
                                                                        FieldValue.of(
                                                                            BlogStatusEnum.NORMAL.getCode()),
                                                                        FieldValue.of(
                                                                            BlogStatusEnum.SENSITIVE_FILTER.getCode()),
                                                                        FieldValue.of(
                                                                            BlogStatusEnum.HIDE.getCode()))))))
                                    .mustNot(
                                        not ->
                                            not.term(
                                                term ->
                                                    term.field("deleted").value(true)))
                                    .mustNot(
                                        not ->
                                            not.exists(
                                                exists ->
                                                    exists.field("offlineAt")))))
                .withPageable(PageRequest.of(page - 1, pageSize))
                .withSort(sort -> sort.field(field -> field.field("created").order(SortOrder.Desc)))
                .build();
        SearchHits<@NonNull BlogDocument> hits =
            elasticsearchTemplate.search(
                query, BlogDocument.class, IndexCoordinates.of(indexName));
        return publicPage(hits, page, pageSize);
    }

    @Override
    public Optional<PublicBlogRead> findPublic(long blogId) {
        BlogDocument document =
            elasticsearchTemplate.get(
                Long.toString(blogId), BlogDocument.class, IndexCoordinates.of(indexName));
        if (document == null || Boolean.TRUE.equals(document.getDeleted())) {
            return Optional.empty();
        }
        return Optional.of(toPublic(document));
    }

    @Override
    public RecommendationResult recommendations(Long blogId, int limit) {
        List<PublicBlogRead> hot = recommendHot(limit);
        List<PublicBlogRead> related =
            blogId == null ? List.of() : recommendRelated(blogId, limit);
        return new RecommendationResult(hot, related);
    }

    @Override
    public BlogSearchResult searchPrivate(PrivateBlogSearchQuery query) {
        NativeQuery searchQuery =
            PrivateSearchQueryConvertor.searchConvert(
                query.keywords(),
                query.status(),
                query.createStart(),
                query.createEnd(),
                query.userId(),
                query.allData(),
                query.page(),
                query.pageSize());
        SearchHits<@NonNull BlogDocument> hits =
            elasticsearchTemplate.search(searchQuery, BlogDocument.class, IndexCoordinates.of(indexName));
        return new BlogSearchResult(
            hits.getTotalHits(),
            query.page(),
            query.pageSize(),
            hits.getSearchHits().stream().map(hit -> hit.getContent().getId()).toList());
    }

    @Override
    public long countPrivate(PrivateBlogSearchQuery query) {
        NativeQuery countQuery =
            PrivateSearchQueryConvertor.countConvert(
                query.keywords(),
                query.status(),
                query.createStart(),
                query.createEnd(),
                query.userId(),
                query.allData());
        return elasticsearchTemplate.count(countQuery, BlogDocument.class, IndexCoordinates.of(indexName));
    }

    @Override
    public void updateReadCounts(List<BlogReadCount> counts) {
        if (counts.isEmpty()) {
            return;
        }
        List<UpdateQuery> queries = counts.stream()
            .map(count -> scriptedUpdate(count.blogId(), readCountScript,
                Map.of("readCount", count.readCount())))
            .toList();
        elasticsearchTemplate.bulkUpdate(queries, IndexCoordinates.of(indexName));
    }

    @Override
    public void write(BlogIndexChange change) {
        BlogDocument document = toDocument(change);
        Document source = elasticsearchTemplate.getElasticsearchConverter().mapObject(document);
        elasticsearchTemplate.update(
            scriptedUpdate(document.getId(), contentScript, Map.of("blog", source)),
            IndexCoordinates.of(indexName));
    }

    @Override
    public void writeAdmin(BlogAdminIndexChange message) {
        AdminBlogDocument current =
            elasticsearchTemplate.get(
                Long.toString(message.blog().id()),
                AdminBlogDocument.class,
                IndexCoordinates.of(adminIndexName));
        if (current != null
            && current.getAdminRevision() != null
            && message.blog().adminRevision() < current.getAdminRevision()) {
            return;
        }
        AdminBlogProjection projection = adminProjection(current, message);
        AdminBlogDocument admin = current == null ? new AdminBlogDocument() : current;
        admin.apply(projection);
        elasticsearchTemplate.save(admin, IndexCoordinates.of(adminIndexName));

        if (message.revision() != null) {
            AdminRevisionDocument revision = new AdminRevisionDocument();
            revision.apply(
                message.revision().id(),
                message.revision().blogId(),
                message.revision().versionNo(),
                message.revision().origin(),
                message.revision().baseRevisionId(),
                message.revision().title(),
                message.revision().description(),
                message.revision().content(),
                message.revision().link(),
                message.revision().renderMode(),
                message.revision().firstPublishedAt() == null
                    ? null
                    : ZonedDateTime.of(message.revision().firstPublishedAt(), ZONE_ID),
                message.revision().createdBy(),
                ZonedDateTime.of(message.revision().createdAt(), ZONE_ID));
            elasticsearchTemplate.save(
                revision, IndexCoordinates.of(revisionIndexName));
        }
    }

    @Override
    public Page<AdminBlogRead> searchAdmin(PrivateBlogSearchQuery query) {
        NativeQuery nativeQuery = adminQuery(query);
        SearchHits<@NonNull AdminBlogDocument> hits =
            elasticsearchTemplate.search(
                nativeQuery, AdminBlogDocument.class, IndexCoordinates.of(adminIndexName));
        boolean draftMode =
            query.draftOnly()
                || Objects.equals(query.status(), BlogStatusEnum.DRAFT.getCode());
        return new Page<>(
            hits.getSearchHits().stream()
                .map(hit -> toAdmin(hit.getContent(), draftMode))
                .toList(),
            hits.getTotalHits(),
            query.page(),
            query.pageSize(),
            query.page() == 1,
            hits.getSearchHits().size() < query.pageSize(),
            hits.getTotalHits() == 0,
            pageCount(hits.getTotalHits(), query.pageSize()));
    }

    @Override
    public Page<AdminRevisionRead> searchRevisions(RevisionSearchQuery query) {
        NativeQuery nativeQuery = revisionQuery(query);
        AdminBlogDocument blog =
            elasticsearchTemplate.get(
                Long.toString(query.blogId()),
                AdminBlogDocument.class,
                IndexCoordinates.of(adminIndexName));
        Long currentPublishedRevisionId =
            blog == null ? null : blog.getPublishedRevisionId();
        SearchHits<@NonNull AdminRevisionDocument> hits =
            elasticsearchTemplate.search(
                nativeQuery,
                AdminRevisionDocument.class,
                IndexCoordinates.of(revisionIndexName));
        return new Page<>(
            hits.getSearchHits().stream()
                .map(
                    hit ->
                        toRevision(
                            hit.getContent(), currentPublishedRevisionId))
                .toList(),
            hits.getTotalHits(),
            query.page(),
            query.pageSize(),
            query.page() == 1,
            hits.getSearchHits().size() < query.pageSize(),
            hits.getTotalHits() == 0,
            pageCount(hits.getTotalHits(), query.pageSize()));
    }

    private static UpdateQuery scriptedUpdate(long id, String script, Map<String, Object> params) {
        return UpdateQuery.builder(Long.toString(id))
            .withScript(script)
            .withLang(ScriptLanguage.Painless.jsonValue())
            .withParams(params)
            .withScriptedUpsert(true)
            .withUpsert(Document.create())
            .withRetryOnConflict(5)
            .build();
    }

    private static String script(String name) {
        try {
            return new ClassPathResource("script/" + name).getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException failure) {
            throw new UncheckedIOException("Cannot load search update script " + name, failure);
        }
    }

    private static Page<BlogSearchHit> toPage(
        SearchHits<@NonNull BlogDocument> hits, int page, int pageSize) {
        long totalElements = hits.getTotalHits();
        int totalPages = (int) ((totalElements + pageSize - 1) / pageSize);
        return new Page<>(
            hits.getSearchHits().stream()
                .map(
                    hit -> {
                        BlogDocument document = hit.getContent();
                        return new BlogSearchHit(
                            document.getId(),
                            document.getUserId(),
                            document.getStatus(),
                            document.getTitle(),
                            document.getDescription(),
                            document.getContent(),
                            document.getCreated().toLocalDateTime(),
                            hit.getScore(),
                            hit.getHighlightFields());
                    })
                .toList(),
            totalElements,
            page,
            pageSize,
            page == 1,
            page == totalPages,
            totalElements == 0,
            totalPages);
    }

    static BlogDocument toDocument(BlogIndexChange change) {
        BlogIndexEntry blog = change.blog();
        if (change.operation() == BlogIndexChange.Operation.REMOVE) {
            return BlogDocument.builder()
                .id(blog.id())
                .revision(change.revision())
                .deleted(true)
                .build();
        }
        return BlogDocument.builder()
            .id(blog.id())
            .userId(blog.userId())
            .title(blog.title())
            .description(blog.description())
            .content(blog.content())
            .link(blog.link())
            .readCount(blog.readCount())
            .status(blog.status())
            .created(ZonedDateTime.of(blog.created(), ZONE_ID))
            .updated(ZonedDateTime.of(blog.updated(), ZONE_ID))
            .revision(change.revision())
            .deleted(false)
            .publishedRevisionId(blog.publishedRevisionId())
            .versionNo(blog.versionNo())
            .accessMode(blog.accessMode())
            .renderMode(blog.renderMode())
            .publishedAt(
                blog.publishedAt() == null
                    ? null
                    : ZonedDateTime.of(blog.publishedAt(), ZONE_ID))
            .offlineAt(
                blog.offlineAt() == null
                    ? null
                    : ZonedDateTime.of(blog.offlineAt(), ZONE_ID))
            .sensitiveContent(blog.sensitiveContent())
            .build();
    }

    private List<PublicBlogRead> recommendHot(int limit) {
        NativeQuery query =
            NativeQuery.builder()
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
                                                                        "return Math.log(doc['readCount'].value + 1);")))))
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
                                                                            .origin("now")
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
                                    .boostMode(
                                        FunctionBoostMode.Sum)))
                .withPageable(PageRequest.of(0, limit))
                .build();
        SearchHits<@NonNull BlogDocument> hits =
            elasticsearchTemplate.search(
                query, BlogDocument.class, IndexCoordinates.of(indexName));
        return hits.getSearchHits().stream().map(hit -> toPublic(hit.getContent())).toList();
    }

    private List<PublicBlogRead> recommendRelated(long blogId, int limit) {
        BlogDocument source =
            elasticsearchTemplate.get(
                Long.toString(blogId), BlogDocument.class, IndexCoordinates.of(indexName));
        if (source == null || source.getTitle() == null) {
            return List.of();
        }
        String text =
            String.join(
                " ",
                source.getTitle(),
                source.getDescription() == null ? "" : source.getDescription(),
                source.getContent() == null ? "" : source.getContent());
        NativeQuery query =
            NativeQuery.builder()
                .withQuery(
                    value ->
                        value.bool(
                            bool ->
                                bool.must(
                                        must ->
                                            must.multiMatch(
                                                multi ->
                                                    multi.query(text)
                                                        .fields(
                                                            "title^2",
                                                            "description^1.5",
                                                            "content")))
                                    .filter(
                                        filter ->
                                            filter.terms(
                                                terms ->
                                                    terms.field("status")
                                                        .terms(
                                                            values ->
                                                                values.value(
                                                                    List.of(
                                                                        FieldValue.of(
                                                                            BlogStatusEnum.NORMAL.getCode()),
                                                                        FieldValue.of(
                                                                            BlogStatusEnum.SENSITIVE_FILTER.getCode()))))))
                                    .mustNot(
                                        not ->
                                            not.ids(
                                                ids ->
                                                    ids.values(
                                                        Long.toString(blogId))))
                                    .mustNot(
                                        not ->
                                            not.term(
                                                term ->
                                                    term.field("deleted").value(true)))))
                .withPageable(PageRequest.of(0, limit))
                .build();
        SearchHits<@NonNull BlogDocument> hits =
            elasticsearchTemplate.search(
                query, BlogDocument.class, IndexCoordinates.of(indexName));
        return hits.getSearchHits().stream().map(hit -> toPublic(hit.getContent())).toList();
    }

    private static NativeQuery adminQuery(PrivateBlogSearchQuery query) {
        boolean draftOnly = query.draftOnly()
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
                                                        match.field("publishedTitle")
                                                            .query(keyword)))
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
                        field ->
                            field.field("created")
                                .order(
                                    SortOrder.Desc)))
            .build();
    }

    private static NativeQuery revisionQuery(RevisionSearchQuery query) {
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
                        field ->
                            field.field("versionNo")
                                .order(
                                    SortOrder.Desc)))
            .build();
    }

    private static AdminBlogProjection adminProjection(
        AdminBlogDocument current, BlogAdminIndexChange message) {
        var blog = message.blog();
        var draft = message.draft();
        var revision = message.revision();
        Long publishedRevisionId =
            blog.publishedRevisionId() != null
                ? blog.publishedRevisionId()
                : current == null ? null : current.getPublishedRevisionId();
        boolean revisionIsPublished =
            revision != null && Objects.equals(revision.id(), publishedRevisionId);
        String publishedTitle =
            revisionIsPublished
                ? revision.title()
                : existing(current, AdminBlogDocument::getPublishedTitle);
        String publishedDescription =
            revisionIsPublished
                ? revision.description()
                : existing(current, AdminBlogDocument::getPublishedDescription);
        String publishedContent =
            revisionIsPublished
                ? revision.content()
                : existing(current, AdminBlogDocument::getPublishedContent);
        String publishedLink =
            revisionIsPublished
                ? revision.link()
                : existing(current, AdminBlogDocument::getPublishedLink);
        BlogRenderMode renderMode =
            revisionIsPublished
                ? revision.renderMode()
                : existing(current, AdminBlogDocument::getRenderMode);
        ZonedDateTime publishedAt =
            blog.publishedAt() == null ? null : ZonedDateTime.of(blog.publishedAt(), ZONE_ID);
        ZonedDateTime offlineAt =
            blog.offlineAt() == null ? null : ZonedDateTime.of(blog.offlineAt(), ZONE_ID);
        boolean hasDraft = draft != null;
        boolean draftNewer =
            draft != null
                && Objects.equals(draft.basePublishedRevisionId(), publishedRevisionId)
                && publishedAt != null
                && draft.updatedAt().isAfter(blog.publishedAt());
        Integer status =
            publishedRevisionId == null
                ? BlogStatusEnum.DRAFT.getCode()
                : offlineAt != null || Objects.equals(blog.accessMode(), 1)
                    ? BlogStatusEnum.HIDE.getCode()
                    : renderMode == BlogRenderMode.SENSITIVE_FILTER
                        ? BlogStatusEnum.SENSITIVE_FILTER.getCode()
                        : BlogStatusEnum.NORMAL.getCode();
        return new AdminBlogProjection(
            blog.id(),
            blog.userId(),
            publishedRevisionId,
            blog.publicationRevision(),
            blog.adminRevision(),
            blog.readCount(),
            ZonedDateTime.of(blog.createdAt(), ZONE_ID),
            publishedAt,
            offlineAt,
            blog.accessMode(),
            renderMode,
            draft == null ? null : draft.draftRevision(),
            draft == null ? null : draft.basePublishedRevisionId(),
            hasDraft,
            draftNewer,
            draft == null
                ? null
                : ZonedDateTime.of(draft.updatedAt(), ZONE_ID),
            publishedTitle,
            publishedDescription,
            publishedContent,
            publishedLink,
            draft == null ? null : draft.title(),
            draft == null ? null : draft.description(),
            draft == null ? null : draft.content(),
            draft == null ? null : draft.link(),
            status,
            "DELETE".equals(message.operation()));
    }

    private static <T> T existing(
        AdminBlogDocument document, Function<AdminBlogDocument, T> getter) {
        return document == null ? null : getter.apply(document);
    }

    private static PublicBlogRead toPublic(BlogDocument document) {
        return new PublicBlogRead(
            document.getId(),
            document.getUserId(),
            document.getPublishedRevisionId(),
            document.getVersionNo(),
            document.getTitle(),
            document.getDescription(),
            document.getContent(),
            document.getLink(),
            document.getReadCount(),
            document.getCreated() == null ? null : document.getCreated().toLocalDateTime(),
            document.getPublishedAt() == null
                ? null
                : document.getPublishedAt().toLocalDateTime(),
            document.getOfflineAt() == null
                ? null
                : document.getOfflineAt().toLocalDateTime(),
            document.getAccessMode(),
            document.getRenderMode(),
            document.getSensitiveContent() == null
                ? List.of()
                : document.getSensitiveContent());
    }

    private static AdminBlogRead toAdmin(AdminBlogDocument document, boolean draftMode) {
        return new AdminBlogRead(
            document.getId(),
            document.getUserId(),
            document.getPublishedRevisionId(),
            document.getPublicationRevision(),
            document.getAdminRevision(),
            draftMode ? document.getDraftTitle() : document.getPublishedTitle(),
            draftMode ? document.getDraftDescription() : document.getPublishedDescription(),
            draftMode ? document.getDraftContent() : document.getPublishedContent(),
            draftMode ? document.getDraftLink() : document.getPublishedLink(),
            document.getReadCount(),
            document.getCreated() == null ? null : document.getCreated().toLocalDateTime(),
            document.getDraftNewer() && document.getDraftUpdatedAt() != null
                ? document.getDraftUpdatedAt().toLocalDateTime()
                : document.getPublishedAt() == null
                    ? document.getCreated().toLocalDateTime()
                    : document.getPublishedAt().toLocalDateTime(),
            document.getStatus(),
            document.getAccessMode(),
            document.getRenderMode(),
            Boolean.TRUE.equals(document.getHasDraft()),
            Boolean.TRUE.equals(document.getDraftNewer()),
            document.getDraftRevision(),
            document.getBasePublishedRevisionId(),
            document.getDraftUpdatedAt() == null
                ? null
                : document.getDraftUpdatedAt().toLocalDateTime());
    }

    private static AdminRevisionRead toRevision(
        AdminRevisionDocument document, Long currentPublishedRevisionId) {
        return new AdminRevisionRead(
            document.getId(),
            document.getBlogId(),
            document.getVersionNo(),
            document.getOrigin(),
            document.getBaseRevisionId(),
            document.getTitle(),
            document.getDescription(),
            document.getContent(),
            document.getLink(),
            document.getRenderMode(),
            document.getFirstPublishedAt() == null
                ? null
                : document.getFirstPublishedAt().toLocalDateTime(),
            document.getCreatedBy(),
            document.getCreatedAt().toLocalDateTime(),
            document.getId().equals(currentPublishedRevisionId));
    }

    private static Page<PublicBlogRead> publicPage(
        SearchHits<@NonNull BlogDocument> hits, int page, int pageSize) {
        return new Page<>(
            hits.getSearchHits().stream().map(hit -> toPublic(hit.getContent())).toList(),
            hits.getTotalHits(),
            page,
            pageSize,
            page == 1,
            hits.getSearchHits().size() < pageSize,
            hits.getTotalHits() == 0,
            pageCount(hits.getTotalHits(), pageSize));
    }

    private static int pageCount(long total, int pageSize) {
        return (int) ((total + pageSize - 1) / pageSize);
    }
}
