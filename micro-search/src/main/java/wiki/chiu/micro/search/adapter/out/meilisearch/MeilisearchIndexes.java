package wiki.chiu.micro.search.adapter.out.meilisearch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpMethod;

import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogRevisionIndexEntry;
import wiki.chiu.micro.search.application.model.IndexTransition;
import wiki.chiu.micro.search.application.model.SearchIndexKind;
import wiki.chiu.micro.search.application.port.out.BlogIndexMaintenance;

public final class MeilisearchIndexes implements BlogIndexMaintenance {
    private final MeilisearchClient client;
    private final Map<SearchIndexKind, String> names;
    private final int maxHits;

    public MeilisearchIndexes(MeilisearchClient client, String blogs, String admin, String revisions, int maxHits) {
        this.client = client;
        this.names = Map.of(SearchIndexKind.PUBLIC_BLOGS, blogs, SearchIndexKind.ADMIN_BLOGS, admin,
            SearchIndexKind.REVISIONS, revisions);
        if (names.values().stream().distinct().count() != names.size()
            || names.values().stream().anyMatch(name -> !name.matches("[A-Za-z0-9_-]+"))) {
            throw new IllegalArgumentException("Expected three distinct Meilisearch index UIDs");
        }
        this.maxHits = maxHits;
    }

    public void initialize() {
        names.forEach((kind, name) -> configure(kind, name));
    }

    @Override
    public Map<SearchIndexKind, String> currentIndexes() {
        return names;
    }

    @Override
    public String createIndex(SearchIndexKind kind) {
        String name = names.get(kind) + "-" + UUID.randomUUID();
        configure(kind, name);
        return name;
    }

    private void configure(SearchIndexKind kind, String name) {
        String path = "/indexes/" + name;
        if (client.lookup(path, "index_not_found") == null) {
            client.task(HttpMethod.POST, "/indexes", Map.of("uid", name, "primaryKey", "id"));
        }
        List<String> text = List.of("title", "description", "content");
        List<String> fields = switch (kind) {
            case PUBLIC_BLOGS, REVISIONS -> text;
            case ADMIN_BLOGS -> List.of("publishedTitle", "publishedDescription", "publishedContent",
                "draftTitle", "draftDescription", "draftContent");
        };
        var settings = new HashMap<String, Object>();
        settings.put("searchableAttributes", fields);
        settings.put("localizedAttributes", List.of(Map.of("attributePatterns", fields, "locales", List.of("cmn"))));
        settings.put("pagination", Map.of("maxTotalHits", maxHits));
        switch (kind) {
            case PUBLIC_BLOGS -> {
                settings.put("filterableAttributes", List.of("deleted", "status"));
                settings.put("rankingRules", List.of("words", "typo", "proximity", "attribute", "exactness",
                    "readCount:desc", "updated:desc"));
            }
            case ADMIN_BLOGS -> {
                settings.put("filterableAttributes", List.of("deleted", "status", "ownerKey", "createdMillis",
                    "hasDraft", "publishedRevisionId"));
                settings.put("sortableAttributes", List.of("createdMillis", "id"));
            }
            case REVISIONS -> {
                settings.put("filterableAttributes", List.of("articleKey", "ownerKey"));
                settings.put("sortableAttributes", List.of("versionNo"));
            }
        }
        if (kind != SearchIndexKind.PUBLIC_BLOGS) {
            settings.put("rankingRules", List.of("sort", "words", "typo", "proximity", "attribute", "exactness"));
        }
        client.task(HttpMethod.PATCH, path + "/settings", settings);
    }

    @Override
    public void writePublicSnapshots(String name, List<BlogIndexChange> rows) {
        client.replace(name, rows.stream().map(BlogDocumentConvertor::publicDocument).toList());
    }

    @Override
    public void writeAdminSnapshots(String name, List<BlogAdminIndexChange> rows) {
        client.replace(name, rows.stream().map(BlogDocumentConvertor::adminDocument).toList());
    }

    @Override
    public void writeRevisionSnapshots(String name, List<BlogRevisionIndexEntry> rows) {
        client.replace(name, rows.stream().map(BlogDocumentConvertor::toSnapshot)
            .map(BlogDocumentConvertor::revisionDocument).toList());
    }

    @Override
    public long refreshAndCount(SearchIndexKind kind, String index) {
        return client.request(HttpMethod.GET, "/indexes/" + index + "/stats", null)
            .required("numberOfDocuments").asLong();
    }

    @Override
    public List<IndexTransition> activate(List<IndexTransition> transitions) {
        client.task(HttpMethod.POST, "/swap-indexes", transitions.stream().map(transition ->
            Map.of("indexes", List.of(transition.previousIndex(), transition.index()))).toList());
        // The previous data is now accessible under the staging UID.
        return transitions.stream().map(t -> new IndexTransition(t.kind(), t.index(), t.previousIndex(), t.documents())).toList();
    }
}
