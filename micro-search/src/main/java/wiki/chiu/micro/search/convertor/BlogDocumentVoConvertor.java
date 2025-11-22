package wiki.chiu.micro.search.convertor;

import org.jspecify.annotations.NonNull;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.document.BlogDocument;
import wiki.chiu.micro.search.vo.BlogDocumentVo;
import org.springframework.data.elasticsearch.core.SearchHits;

import java.util.List;

public class BlogDocumentVoConvertor {

    private BlogDocumentVoConvertor() {}

    public static PageAdapter<BlogDocumentVo> convert(SearchHits<@NonNull BlogDocument> search, Integer blogPageSize, Integer currentPage) {
        long totalHits = search.getTotalHits();
        long totalPage = totalHits % blogPageSize == 0 ? totalHits / blogPageSize : totalHits / blogPageSize + 1;

        List<BlogDocumentVo> vos = search.getSearchHits().stream()
                .map(hit -> BlogDocumentVo.builder()
                        .id(hit.getContent().getId())
                        .userId(hit.getContent().getUserId())
                        .status(hit.getContent().getStatus())
                        .title(hit.getContent().getTitle())
                        .description(hit.getContent().getDescription())
                        .content(hit.getContent().getContent())
                        .created(hit.getContent().getCreated().toLocalDateTime())
                        .score(hit.getScore())
                        .highlight(hit.getHighlightFields())
                        .build())
                .toList();

        return PageAdapter.<BlogDocumentVo>builder()
                .first(currentPage == 1)
                .last(currentPage == totalPage)
                .pageSize(blogPageSize)
                .pageNumber(currentPage)
                .empty(totalHits == 0)
                .totalElements(totalHits)
                .totalPages((int) totalPage)
                .content(vos)
                .build();
    }
}
