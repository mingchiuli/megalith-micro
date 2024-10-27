package wiki.chiu.micro.search.rpc;

import wiki.chiu.micro.common.dto.BlogEntityRpcDto;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.rpc.BlogHttpService;
import org.springframework.stereotype.Component;

@Component
public class BlogHttpServiceWrapper {

    private final BlogHttpService blogHttpService;

    public BlogHttpServiceWrapper(BlogHttpService blogHttpService) {
        this.blogHttpService = blogHttpService;
    }

    public BlogEntityRpcDto findById(Long blogId) {
        Result<BlogEntityRpcDto> result = blogHttpService.findById(blogId);
        if (result.code() != 200) {
            throw new MissException(result.msg());
        }
        return result.data();
    }
}