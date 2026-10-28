package wiki.chiu.micro.blog.application.service;

import java.util.List;

import wiki.chiu.micro.blog.application.model.IndexSourceStatus;
import wiki.chiu.micro.blog.application.port.in.BlogIndexSourceQueries;
import wiki.chiu.micro.blog.application.port.out.BlogIndexSourceState;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.model.BlogAdminIndexSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSnapshot;

public class BlogIndexSourceService implements BlogIndexSourceQueries {

    private final BlogQueryStore blogs;
    private final BlogIndexSourceState state;

    public BlogIndexSourceService(BlogQueryStore blogs, BlogIndexSourceState state) {
        this.blogs = blogs;
        this.state = state;
    }

    @Override
    public IndexSourceStatus status() {
        return state.status();
    }

    @Override
    public List<BlogSnapshot> snapshots(long afterId, int limit) {
        if (!state.status().ready()) {
            throw new BaseException(CommonErrorCode.CONFLICT,
                "index snapshots require read-only maintenance and an empty BLOG outbox");
        }
        return blogs.findPublishedSnapshotsAfter(afterId, limit);
    }

    @Override
    public List<BlogSnapshot> bootstrapSnapshots(long afterId, int limit) {
        return blogs.findPublishedSnapshotsAfter(afterId, limit);
    }

    @Override
    public List<BlogAdminIndexSnapshot> adminSnapshots(long afterId, int limit) {
        return blogs.findAdminSnapshotsAfter(afterId, limit);
    }

    @Override
    public List<BlogRevisionSnapshot> revisionSnapshots(long afterRevisionId, int limit) {
        return blogs.findRevisionSnapshotsAfter(afterRevisionId, limit);
    }
}
