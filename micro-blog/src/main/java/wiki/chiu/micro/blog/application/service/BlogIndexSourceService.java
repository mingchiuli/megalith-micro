package wiki.chiu.micro.blog.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.blog.application.port.in.BlogIndexSourceQueries;
import wiki.chiu.micro.blog.application.port.out.BlogIndexSourceState;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.convertor.BlogSnapshotConvertor;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.lang.BlogSnapshot;
import wiki.chiu.micro.common.lang.CommonErrorCode;

@Service
public class BlogIndexSourceService implements BlogIndexSourceQueries {

    private final BlogQueryStore blogs;
    private final BlogIndexSourceState state;

    public BlogIndexSourceService(BlogQueryStore blogs, BlogIndexSourceState state) {
        this.blogs = blogs;
        this.state = state;
    }

    @Override
    public BlogIndexSourceStatus status() {
        return state.status();
    }

    @Override
    public List<BlogSnapshot> snapshots(long afterId, int limit) {
        if (!state.status().ready()) {
            throw new BaseException(CommonErrorCode.CONFLICT,
                "index snapshots require read-only maintenance and an empty BLOG outbox");
        }
        return blogs.findSnapshotsAfter(afterId, limit).stream().map(BlogSnapshotConvertor::convert).toList();
    }
}
