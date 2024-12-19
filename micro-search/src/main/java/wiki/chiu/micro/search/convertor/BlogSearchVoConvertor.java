package wiki.chiu.micro.search.convertor;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import wiki.chiu.micro.search.document.BlogDocument;
import wiki.chiu.micro.search.vo.BlogSearchVo;

import java.util.List;

public class BlogSearchVoConvertor {
    public static BlogSearchVo convert(SearchHits<BlogDocument> searchResp, Integer currentPage, Integer size) {
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
}
