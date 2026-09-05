package wiki.chiu.micro.search.adapter.out.http;

import java.util.List;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.api.BlogIndexSourceHttpService;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.IndexSourceStatus;
import wiki.chiu.micro.search.application.port.out.BlogIndexSource;
import wiki.chiu.micro.search.domain.BlogIndexEntry;

@Component
public class BlogIndexSourceHttpAdapter implements BlogIndexSource {

    private final BlogIndexSourceHttpService blogs;

    public BlogIndexSourceHttpAdapter(BlogIndexSourceHttpService blogs) {
        this.blogs = blogs;
    }

    @Override
    public IndexSourceStatus status() {
        var status = RemoteResult.requireSuccess(blogs::indexSourceStatus);
        return new IndexSourceStatus(status.readOnly(), status.readyEvents(), status.pausedEvents(), status.total());
    }

    @Override
    public List<BlogIndexChange> snapshots(long afterId, int limit) {
        return RemoteResult.requireSuccess(() -> blogs.indexSnapshots(afterId, limit)).stream()
            .map(blog -> new BlogIndexChange(BlogIndexChange.Operation.CREATE, blog.revision(),
                new BlogIndexEntry(blog.id(), blog.userId(), blog.status(), blog.readCount(),
                    blog.title(), blog.description(), blog.content(), blog.created(), blog.updated())))
            .toList();
    }
}
