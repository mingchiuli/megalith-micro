package wiki.chiu.micro.search.adapter.out.http;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.api.BlogIndexSourceHttpService;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.port.out.BlogBootstrapIndexSource;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

@Component
public class BlogBootstrapIndexSourceHttpAdapter implements BlogBootstrapIndexSource {

    private final BlogIndexSourceHttpService blogs;

    public BlogBootstrapIndexSourceHttpAdapter(BlogIndexSourceHttpService blogs) {
        this.blogs = blogs;
    }

    @Override
    public List<BlogIndexChange> publicSnapshots(long afterId, int limit) {
        return RemoteResult.requireSuccess(
                () -> blogs.indexBootstrapSnapshots(afterId, limit))
            .stream()
            .map(
                blog ->
                    new BlogIndexChange(
                        BlogIndexChange.Operation.REPAIR,
                        blog.revision(),
                        new BlogIndexEntry(
                            blog.id(),
                            blog.userId(),
                            blog.status(),
                            blog.readCount(),
                            blog.title(),
                            blog.description(),
                            blog.content(),
                            blog.link(),
                            blog.created(),
                            blog.updated(),
                            blog.publishedRevisionId(),
                            null,
                            blog.publishedAt(),
                            blog.offlineAt(),
                            blog.accessMode(),
                            BlogRenderMode.of(blog.renderMode()),
                            blog.sensitiveContent() == null
                                ? List.of()
                                : blog.sensitiveContent())))
            .toList();
    }

    @Override
    public List<BlogAdminIndexChange> adminSnapshots(long afterId, int limit) {
        return RemoteResult.requireSuccess(
                () -> blogs.indexAdminSnapshots(afterId, limit))
            .stream()
            .map(
                snapshot ->
                    new BlogAdminIndexChange(
                        "BOOTSTRAP",
                        snapshot.blog(),
                        snapshot.draft(),
                        snapshot.revision()))
            .toList();
    }

    @Override
    public List<BlogAdminIndexChange> revisionSnapshots(long afterRevisionId, int limit) {
        return RemoteResult.requireSuccess(
                () -> blogs.indexRevisionSnapshots(afterRevisionId, limit))
            .stream()
            .map(revision -> new BlogAdminIndexChange("BOOTSTRAP", null, null, revision))
            .toList();
    }
}
