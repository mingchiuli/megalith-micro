package wiki.chiu.micro.blog.rpc;


import wiki.chiu.micro.common.dto.BlogSearchRpcDto;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.req.BlogSysCountSearchReq;
import wiki.chiu.micro.common.req.BlogSysSearchReq;
import wiki.chiu.micro.common.rpc.SearchHttpService;
import org.springframework.stereotype.Component;

@Component
public class SearchHttpServiceWrapper {

    private final SearchHttpService searchHttpService;

    public SearchHttpServiceWrapper(SearchHttpService searchHttpService) {
        this.searchHttpService = searchHttpService;
    }

    public BlogSearchRpcDto searchBlogs(BlogSysSearchReq req) {
        Result<BlogSearchRpcDto> result = searchHttpService.searchBlogs(req);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

    public Long countBlogs(BlogSysCountSearchReq req) {
        Result<Long> result = searchHttpService.countBlogs(req);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }

}
