package wiki.chiu.micro.search.convertor;

import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import wiki.chiu.micro.common.vo.BlogSearchRpcVo;
import wiki.chiu.micro.search.document.BlogDocument;

import java.util.List;

public class BlogSearchRpcVoConvertor {
    public static BlogSearchRpcVo convert(SearchHits<BlogDocument> searchResp, Integer currentPage, Integer size) {
        List<Long> ids = searchResp.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(BlogDocument::getId)
                .toList();

        return BlogSearchRpcVo.builder()
                .ids(ids)
                .currentPage(currentPage)
                .size(size)
                .total(searchResp.getTotalHits())
                .build();
    }
}
