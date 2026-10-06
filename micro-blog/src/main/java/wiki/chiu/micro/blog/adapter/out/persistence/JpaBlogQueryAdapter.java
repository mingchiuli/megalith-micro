package wiki.chiu.micro.blog.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.adapter.out.persistence.mapping.BlogPersistenceMapper;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogSensitiveContentRepository;
import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;

@Component
public class JpaBlogQueryAdapter implements BlogQueryStore {

    private final BlogRepository blogs;
    private final BlogSensitiveContentRepository sensitiveContents;

    public JpaBlogQueryAdapter(
        BlogRepository blogs, BlogSensitiveContentRepository sensitiveContents) {
        this.blogs = blogs;
        this.sensitiveContents = sensitiveContents;
    }

    @Override
    public List<Long> findIdsAfter(Long afterId, int limit) {
        return blogs.findIdsAfter(afterId, PageRequest.of(0, limit));
    }

    @Override
    public List<BlogReadCount> findReadCountsAfter(long afterId, int limit) {
        return blogs.findReadCountsAfter(afterId, PageRequest.of(0, limit));
    }

    @Override
    public List<Blog> findSnapshotsAfter(long afterId, int limit) {
        return BlogPersistenceMapper.toDomains(blogs.findSnapshotsAfter(afterId, PageRequest.of(0, limit)));
    }

    @Override
    public Optional<Blog> findById(Long blogId) {
        return blogs.findById(blogId).map(BlogPersistenceMapper::toDomain);
    }

    @Override
    public List<Blog> findAllById(List<Long> blogIds) {
        return BlogPersistenceMapper.toDomains(blogs.findAllById(blogIds));
    }

    @Override
    public List<Blog> findByUserIds(List<Long> userIds) {
        return BlogPersistenceMapper.toDomains(blogs.findByUserIdIn(userIds));
    }

    @Override
    public long count() {
        return blogs.count();
    }

    @Override
    public Page<Blog> findPage(int pageNumber, int pageSize, List<Integer> statuses) {
        var request = PageRequest.of(pageNumber - 1, pageSize, Sort.by("created").descending());
        var page = blogs.findByStatusIn(request, statuses);
        return new Page<>(
            BlogPersistenceMapper.toDomains(page.getContent()),
            page.getTotalElements(),
            page.getNumber() + 1,
            page.getSize(),
            page.isFirst(),
            page.isLast(),
            page.isEmpty(),
            page.getTotalPages());
    }

    @Override
    public List<SensitiveContent> findSensitiveByBlogId(Long blogId) {
        return BlogPersistenceMapper.toSensitiveDomains(sensitiveContents.findByBlogId(blogId));
    }

    @Override
    public List<SensitiveContent> findSensitiveByBlogIds(List<Long> blogIds) {
        return BlogPersistenceMapper.toSensitiveDomains(sensitiveContents.findByBlogIdIn(blogIds));
    }
}
