package wiki.chiu.micro.blog.rpc;


import wiki.chiu.micro.common.dto.BlogSearchRpcDto;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.BlogSearchReq;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import org.springframework.stereotype.Component;

@Component
public class SearchHttpServiceWrapper {

    private final SearchHttpService searchHttpService;

    public SearchHttpServiceWrapper(SearchHttpService searchHttpService) {
        this.searchHttpService = searchHttpService;
    }

    public BlogSearchRpcDto searchBlogs(BlogSearchReq req) {
        Result<BlogSearchRpcDto> result = searchHttpService.searchBlogs(req.page(), req.pageSize(), req.keywords());
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public Long countBlogs(String keywords) {
        Result<Long> result = searchHttpService.countBlogs(keywords);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

}
