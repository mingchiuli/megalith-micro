package wiki.chiu.micro.blog.adapter.in.http;

import static wiki.chiu.micro.common.web.FunctionalWeb.ok;
import static wiki.chiu.micro.common.web.FunctionalWeb.requiredParam;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import wiki.chiu.micro.blog.api.BlogIndexSourceHttpService;
import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.blog.application.port.in.BlogIndexSourceQueries;
import wiki.chiu.micro.common.lang.BlogSnapshot;
import wiki.chiu.micro.common.lang.Result;
import wiki.chiu.micro.common.web.ValidatedRequest;

@Component
public class BlogIndexSourceHttpHandler implements BlogIndexSourceHttpService {

    private final BlogIndexSourceQueries source;
    private final ValidatedRequest validation;

    public BlogIndexSourceHttpHandler(BlogIndexSourceQueries source, ValidatedRequest validation) {
        this.source = source;
        this.validation = validation;
    }

    public ServerResponse status(ServerRequest request) {
        return ok(indexSourceStatus());
    }

    public ServerResponse snapshots(ServerRequest request) {
        long afterId = validation.nonNegative(requiredParam(request, "afterId", Long::valueOf), "afterId");
        int limit = validation.range(requiredParam(request, "limit", Integer::valueOf), 1, 1000, "limit");
        return ok(indexSnapshots(afterId, limit));
    }

    @Override
    public Result<BlogIndexSourceStatus> indexSourceStatus() {
        return Result.success(source::status);
    }

    @Override
    public Result<List<BlogSnapshot>> indexSnapshots(long afterId, int limit) {
        return Result.success(() -> source.snapshots(afterId, limit));
    }
}
