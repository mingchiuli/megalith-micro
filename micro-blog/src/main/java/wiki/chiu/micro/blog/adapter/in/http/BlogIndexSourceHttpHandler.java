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
import wiki.chiu.micro.common.model.BlogAdminIndexSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSnapshot;
import wiki.chiu.micro.common.result.Result;
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
        long afterId = afterId(request);
        int limit = limit(request);
        return ok(indexSnapshots(afterId, limit));
    }

    public ServerResponse bootstrapSnapshots(ServerRequest request) {
        return ok(indexBootstrapSnapshots(afterId(request), limit(request)));
    }

    public ServerResponse adminSnapshots(ServerRequest request) {
        return ok(indexAdminSnapshots(afterId(request), limit(request)));
    }

    public ServerResponse revisionSnapshots(ServerRequest request) {
        return ok(indexRevisionSnapshots(afterRevisionId(request), limit(request)));
    }

    @Override
    public Result<BlogIndexSourceStatus> indexSourceStatus() {
        return Result.success(() -> BlogRpcMapper.toRpc(source.status()));
    }

    @Override
    public Result<List<BlogSnapshot>> indexSnapshots(long afterId, int limit) {
        return Result.success(() -> source.snapshots(afterId, limit));
    }

    @Override
    public Result<List<BlogSnapshot>> indexBootstrapSnapshots(long afterId, int limit) {
        return Result.success(() -> source.bootstrapSnapshots(afterId, limit));
    }

    @Override
    public Result<List<BlogAdminIndexSnapshot>> indexAdminSnapshots(long afterId, int limit) {
        return Result.success(() -> source.adminSnapshots(afterId, limit));
    }

    @Override
    public Result<List<BlogRevisionSnapshot>> indexRevisionSnapshots(
        long afterRevisionId, int limit) {
        return Result.success(() -> source.revisionSnapshots(afterRevisionId, limit));
    }

    private long afterId(ServerRequest request) {
        return validation.nonNegative(
            requiredParam(request, "afterId", Long::valueOf), "afterId");
    }

    private long afterRevisionId(ServerRequest request) {
        return validation.nonNegative(
            requiredParam(request, "afterRevisionId", Long::valueOf), "afterRevisionId");
    }

    private int limit(ServerRequest request) {
        return validation.range(
            requiredParam(request, "limit", Integer::valueOf), 1, 1000, "limit");
    }
}
