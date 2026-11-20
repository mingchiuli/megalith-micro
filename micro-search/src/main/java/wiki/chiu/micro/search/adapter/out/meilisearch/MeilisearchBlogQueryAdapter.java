package wiki.chiu.micro.search.adapter.out.meilisearch;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.search.application.model.AdminBlogRead;
import wiki.chiu.micro.search.application.model.AdminRevisionRead;
import wiki.chiu.micro.search.application.model.BlogSearchHit;
import wiki.chiu.micro.search.application.model.Page;
import wiki.chiu.micro.search.application.model.PrivateBlogSearchQuery;
import wiki.chiu.micro.search.application.model.PublicBlogSearchQuery;
import wiki.chiu.micro.search.application.model.RecommendationFeed;
import wiki.chiu.micro.search.application.model.RecommendationFeedQuery;
import wiki.chiu.micro.search.application.model.RevisionSearchQuery;
import wiki.chiu.micro.search.application.model.SearchIndexKind;
import wiki.chiu.micro.search.application.port.out.BlogSearchIndex;

public final class MeilisearchBlogQueryAdapter implements BlogSearchIndex {
    private static final List<String> TEXT = List.of("title", "description", "content");
    private final MeilisearchClient client;
    private final Map<SearchIndexKind, String> indexes;
    private final RecommendationSnapshots recommendations;
    private final JsonMapper json;
    private final int publicPageSize;

    public MeilisearchBlogQueryAdapter(MeilisearchClient client, MeilisearchIndexes indexes,
        RecommendationSnapshots recommendations, JsonMapper json, int publicPageSize) {
        this.client = client;
        this.indexes = indexes.currentIndexes();
        this.recommendations = recommendations;
        this.json = json;
        this.publicPageSize = publicPageSize;
    }

    @Override
    public RecommendationFeed recommendationFeed(RecommendationFeedQuery query) {
        return recommendations.page(query);
    }

    @Override
    public Page<BlogSearchHit> searchPublic(PublicBlogSearchQuery query) {
        var params = new HashMap<String, Object>();
        params.put("filter", "deleted = false AND status = " + BlogStatusEnum.NORMAL.getCode());
        params.put("attributesToHighlight", TEXT);
        params.put("highlightPreTag", "<b style='color:red'>");
        params.put("highlightPostTag", "</b>");
        params.put("showRankingScore", true);
        if (!query.allInfo()) {
            params.put("attributesToCrop", TEXT);
            params.put("cropLength", 12);
        }
        return page(SearchIndexKind.PUBLIC_BLOGS, query.keywords(), query.page(), publicPageSize,
            params, hit -> BlogDocumentConvertor.searchHit(json.treeToValue(hit, BlogDocument.class),
                (float) hit.required("_rankingScore").asDouble(), highlights(hit)));
    }

    private Map<String, List<String>> highlights(JsonNode hit) {
        Map<String, List<String>> highlights = new HashMap<>();
        for (String field : TEXT) {
            String fragment = hit.path("_formatted").path(field).asString("");
            if (fragment.contains("<b style='color:red'>")) {
                highlights.put(field, List.of(fragment));
            }
        }
        return highlights;
    }

    @Override
    public Page<AdminBlogRead> searchAdmin(PrivateBlogSearchQuery query) {
        boolean draft = query.draftOnly() || Objects.equals(query.status(), BlogStatusEnum.DRAFT.getCode());
        var filters = new ArrayList<String>();
        filters.add("deleted = false");
        filters.add(draft ? "hasDraft = true" : "publishedRevisionId IS NOT NULL AND publishedRevisionId EXISTS");
        if (!draft && query.status() != null) {
            filters.add("status = " + query.status());
        }
        if (!query.allData()) {
            filters.add("ownerKey = u" + query.userId());
        }
        if (query.createStart() != null) {
            filters.add("createdMillis >= " + millis(query.createStart()));
        }
        if (query.createEnd() != null) {
            filters.add("createdMillis <= " + millis(query.createEnd()));
        }
        var params = new HashMap<String, Object>();
        params.put("filter", filters);
        params.put("attributesToSearchOn", draft ? List.of("draftTitle", "draftDescription", "draftContent")
            : List.of("publishedTitle", "publishedDescription", "publishedContent"));
        params.put("sort", List.of("createdMillis:desc", "id:asc"));
        return page(SearchIndexKind.ADMIN_BLOGS, query.keywords(), query.page(), query.pageSize(),
            params, hit -> BlogDocumentConvertor.adminRead(json.treeToValue(hit, AdminBlogDocument.class), draft));
    }

    @Override
    public Page<AdminRevisionRead> searchRevisions(RevisionSearchQuery query) {
        var blog = client.document(indexes.get(SearchIndexKind.ADMIN_BLOGS), query.blogId());
        Long publishedId = blog == null || !blog.path("publishedRevisionId").isNumber()
            ? null : blog.get("publishedRevisionId").asLong();
        var filters = new ArrayList<String>();
        filters.add("articleKey = b" + query.blogId());
        if (!query.allData()) {
            filters.add("ownerKey = u" + query.userId());
        }
        var params = new HashMap<String, Object>();
        params.put("filter", filters);
        params.put("sort", List.of("versionNo:desc"));
        return page(SearchIndexKind.REVISIONS, query.keywords(), query.page(), query.pageSize(),
            params, hit -> BlogDocumentConvertor.revisionRead(json.treeToValue(hit, AdminRevisionDocument.class), publishedId));
    }

    private static long millis(LocalDateTime date) {
        return date.atZone(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli();
    }

    private <T> Page<T> page(SearchIndexKind kind, String q, int page, int size,
        Map<String, Object> params, Function<JsonNode, T> convert) {
        params.put("q", q == null ? "" : q);
        params.put("page", page);
        params.put("hitsPerPage", size);
        var result = client.search(indexes.get(kind), params);
        return BlogDocumentConvertor.page(result.required("hits").valueStream().map(convert).toList(),
            result.required("totalHits").asLong(), page, size);
    }
}
