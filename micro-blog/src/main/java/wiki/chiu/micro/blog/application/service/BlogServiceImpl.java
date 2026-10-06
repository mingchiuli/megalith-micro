package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.error.ExceptionMessage.NO_FOUND;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import wiki.chiu.micro.blog.application.model.BlogDraft;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.model.BlogEdit;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchResult;
import wiki.chiu.micro.blog.application.model.BlogSearchSelection;
import wiki.chiu.micro.blog.application.model.DeletedBlogEntry;
import wiki.chiu.micro.blog.application.model.DeletedBlogItem;
import wiki.chiu.micro.blog.application.model.DeletedBlogPage;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.model.SensitiveContentDraft;
import wiki.chiu.micro.blog.application.port.in.BlogService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.SensitiveContent;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.exception.MissException;

public class BlogServiceImpl implements BlogService {

    private static final int RECYCLE_RETENTION_DAYS = 7;

    private final BlogQueryStore blogs;

    private final BlogRuntimeStore runtimeStore;

    private final BlogWriter blogWrapper;

    private final BlogSearchGateway blogSearch;

    private final BlogAccessPolicy accessPolicy;

    public BlogServiceImpl(
        BlogQueryStore blogs,
        BlogRuntimeStore runtimeStore,
        BlogWriter blogWrapper,
        BlogSearchGateway blogSearch,
        BlogAccessPolicy accessPolicy) {
        this.blogs = blogs;
        this.runtimeStore = runtimeStore;
        this.blogWrapper = blogWrapper;
        this.blogSearch = blogSearch;
        this.accessPolicy = accessPolicy;
    }

    @Override
    public BlogEdit findEdit(Long id, Long userId, List<DataPermissionEnum> dataPermissions) {
        Blog blog;
        List<SensitiveContent> spans;
        if (id != null) {
            blog = blogs.findById(id).orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
            accessPolicy.requireCollaboration(blog, userId, dataPermissions);
            spans = blogs.findSensitiveByBlogId(id);
        } else {
            blog = Blog.blankFor(userId);
            spans = List.of();
        }

        return new BlogEdit(
            blog.id(),
            blog.userId(),
            blog.title(),
            blog.description(),
            blog.link(),
            blog.content(),
            blog.status(),
            spans.stream()
                .map(span -> new SensitiveContentDraft(span.type(), span.startIndex(), span.endIndex()))
                .toList(),
            accessPolicy.permissions(blog, userId, dataPermissions));
    }

    @Override
    public void saveOrUpdate(
        BlogDraft blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        Blog current = currentState(blog, userId, dataPermissions);
        Long expectedRevision = blog.isNew() ? null : current.eventRevision();
        Blog candidate = blog.mergeInto(current);
        if (expectedRevision != null) {
            candidate = candidate.withUpdated(LocalDateTime.now());
        }

        List<Long> existingSensitiveIds =
            blog.isNew()
                ? List.of()
                : blogs.findSensitiveByBlogId(blog.id()).stream()
                    .map(SensitiveContent::id)
                    .toList();
        List<SensitiveContent> sensitiveContents =
            blog.sensitiveContents().stream().distinct().toList();
        BlogOperateEnum operation =
            expectedRevision == null ? BlogOperateEnum.CREATE : BlogOperateEnum.UPDATE;

        blogWrapper.saveOrUpdate(
            candidate,
            expectedRevision,
            existingSensitiveIds,
            sensitiveContents,
            new BlogEventContext(operation, userId));
    }

    private Blog currentState(
        BlogDraft blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        if (blog.isNew()) {
            return Blog.newBy(userId);
        }
        Blog existing =
            blogs.findById(blog.id()).orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        accessPolicy.requireEdit(existing, userId, dataPermissions);
        return existing;
    }

    @Override
    public Page<BlogListItem> findAllBlogs(
        BlogQuery query, Long userId, List<DataPermissionEnum> dataPermissions) {

        BlogSearchQuery searchQuery =
            new BlogSearchQuery(
                query.currentPage(),
                query.size(),
                query.keywords(),
                new BlogSearchSelection(
                    query.status(),
                    query.createStart(),
                    query.createEnd(),
                    userId,
                    dataPermissions.contains(DataPermissionEnum.BLOG_VIEW_ALL)));
        BlogSearchResult result = blogSearch.searchBlogs(searchQuery);
        List<Long> ids = result.ids();
        if (ids.isEmpty()) {
            return listPage(List.of(), Map.of(), List.of(), result);
        }

        Map<Long, Integer> order = new HashMap<>();
        for (int index = 0; index < ids.size(); index++) {
            order.put(ids.get(index), index);
        }

        List<Blog> items =
            blogs.findAllById(ids).stream()
                .filter(searchQuery.selection()::includes)
                .sorted(Comparator.comparing(item -> order.get(item.id())))
                .toList();

        List<Long> currentIds = items.stream().map(Blog::id).toList();

        List<SensitiveContent> sensitiveContents =
            currentIds.isEmpty() ? List.of() : blogs.findSensitiveByBlogIds(currentIds);

        Map<Long, Integer> readMap =
            currentIds.isEmpty() ? Map.of() : runtimeStore.readCounts(currentIds);

        return listPage(items, readMap, sensitiveContents, result);
    }

    private static Page<BlogListItem> listPage(
        List<Blog> items,
        Map<Long, Integer> readMap,
        List<SensitiveContent> sensitiveContents,
        BlogSearchResult result) {

        Integer size = result.pageSize();
        Integer currentPage = result.currentPage();
        Long total = result.total();

        Map<Long, LocalDateTime> blogDates =
            items.stream().collect(Collectors.toMap(Blog::id, Blog::updated));
        Map<Long, LocalDateTime> sensitiveDates =
            sensitiveContents.stream()
                .collect(
                    Collectors.toMap(
                        SensitiveContent::blogId,
                        SensitiveContent::updated,
                        (left, right) -> left.isAfter(right) ? left : right));
        Map<Long, LocalDateTime> mergedDates =
            Stream.of(sensitiveDates, blogDates)
                .flatMap(map -> map.entrySet().stream())
                .collect(
                    HashMap::new,
                    (merged, entry) ->
                        merged.merge(
                            entry.getKey(), entry.getValue(), (l, r) -> l.isAfter(r) ? l : r),
                    HashMap::putAll);

        List<BlogListItem> content =
            items.stream()
                .map(
                    blog ->
                        new BlogListItem(
                            blog.id(),
                            blog.title(),
                            blog.description(),
                            blog.content(),
                            blog.link(),
                            blog.readCount(),
                            readMap.getOrDefault(blog.id(), 0),
                            blog.created(),
                            mergedDates.get(blog.id()),
                            blog.status()))
                .toList();

        long anchor = (long) (currentPage - 1) * size + items.size();
        return new Page<>(
            content,
            total,
            currentPage,
            size,
            currentPage == 1,
            anchor >= total,
            items.isEmpty(),
            (int) (total % size == 0 ? total / size : total / size + 1));
    }

    @Override
    public Page<DeletedBlogItem> findDeletedBlogs(
        Integer currentPage, Integer size, Long userId) {
        DeletedBlogPage deleted =
            runtimeStore.deletedBlogs(
                userId, currentPage, size, LocalDateTime.now().minusDays(RECYCLE_RETENTION_DAYS));
        if (deleted.blogs().isEmpty()) {
            return Page.emptyPage();
        }

        int totalPages = (int) (deleted.total() % size == 0 ? deleted.total() / size : deleted.total() / size + 1);
        List<DeletedBlogItem> content = new ArrayList<>();
        int index = deleted.expiredCount();
        for (Blog item : deleted.blogs()) {
            content.add(
                new DeletedBlogItem(
                    item.id(),
                    item.userId(),
                    item.title(),
                    item.description(),
                    item.content(),
                    item.created(),
                    item.updated(),
                    item.status(),
                    index++,
                    item.link(),
                    item.readCount()));
        }

        return new Page<>(
            content,
            deleted.total(),
            currentPage,
            size,
            currentPage == 1,
            currentPage == totalPages,
            deleted.total() == 0,
            totalPages);
    }

    @Override
    public void recoverDeletedBlog(Integer idx, Long userId) {
        DeletedBlogEntry deleted = runtimeStore.deletedBlog(userId, idx).orElse(null);
        if (deleted == null) {
            return;
        }
        blogWrapper.recoverDeletedBlog(
            deleted.blog(), new BlogEventContext(BlogOperateEnum.CREATE, userId));
        runtimeStore.removeDeletedBlog(userId, deleted.receipt());
    }

    @Override
    public void deleteBatch(List<Long> ids, Long userId, List<DataPermissionEnum> dataPermissions) {
        List<Blog> deleted =
            blogs.findAllById(ids).stream()
                .filter(blog -> accessPolicy.canDelete(blog, userId, dataPermissions))
                .toList();
        deletePrepared(deleted, userId);
    }

    @Override
    public void deleteByUserIds(List<Long> userIds) {
        deletePrepared(blogs.findByUserIds(userIds), null);
    }

    private void deletePrepared(List<Blog> deleted, Long operatorUserId) {
        if (deleted.isEmpty()) {
            return;
        }
        List<Blog> nextRevision =
            deleted.stream().map(blog -> blog.withEventRevision(blog.eventRevision() + 1)).toList();
        List<Long> deletedIds = nextRevision.stream().map(Blog::id).toList();
        List<Long> sensitiveIds =
            blogs.findSensitiveByBlogIds(deletedIds).stream().map(SensitiveContent::id).toList();
        blogWrapper.deleteByIds(
            nextRevision,
            sensitiveIds,
            new BlogEventContext(
                BlogOperateEnum.REMOVE, operatorUserId));
    }

}
