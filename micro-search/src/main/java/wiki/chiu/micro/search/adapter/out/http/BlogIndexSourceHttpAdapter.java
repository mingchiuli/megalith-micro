package wiki.chiu.micro.search.adapter.out.http;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.api.BlogIndexSourceHttpService;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogRevisionIndexEntry;
import wiki.chiu.micro.search.application.model.IndexSourceStatus;
import wiki.chiu.micro.search.application.port.out.BlogIndexSource;

@Component
public class BlogIndexSourceHttpAdapter implements BlogIndexSource {

    private final BlogIndexSourceHttpService blogs;

    public BlogIndexSourceHttpAdapter(BlogIndexSourceHttpService blogs) {
        this.blogs = blogs;
    }

    @Override
    public IndexSourceStatus status() {
        var status = RemoteResult.requireSuccess(blogs::indexSourceStatus);
        return BlogIndexSourceConvertor.toStatus(status);
    }

    @Override
    public List<BlogIndexChange> snapshots(long afterId, int limit) {
        return RemoteResult.requireSuccess(() -> blogs.indexSnapshots(afterId, limit)).stream()
            .map(BlogIndexSourceConvertor::toChange)
            .toList();
    }

    @Override
    public List<BlogAdminIndexChange> adminSnapshots(long afterId, int limit) {
        return RemoteResult.requireSuccess(
                () -> blogs.indexAdminSnapshots(afterId, limit))
            .stream()
            .map(
                BlogIndexSourceConvertor::toAdminChange)
            .toList();
    }

    @Override
    public List<BlogRevisionIndexEntry> revisionSnapshots(
        long afterRevisionId, int limit) {
        return RemoteResult.requireSuccess(
                () -> blogs.indexRevisionSnapshots(afterRevisionId, limit))
            .stream()
            .map(
                BlogIndexSourceConvertor::toRevision)
            .toList();
    }
}
