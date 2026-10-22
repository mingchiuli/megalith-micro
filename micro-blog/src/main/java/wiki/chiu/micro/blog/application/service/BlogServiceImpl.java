package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.error.ExceptionMessage.NO_FOUND;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import wiki.chiu.micro.blog.application.model.BlogContentCommand;
import wiki.chiu.micro.blog.application.model.BlogDraft;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.model.BlogEdit;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogPermissions;
import wiki.chiu.micro.blog.application.model.BlogQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchSelection;
import wiki.chiu.micro.blog.application.model.BlogVersionComparison;
import wiki.chiu.micro.blog.application.model.BlogVersionSummary;
import wiki.chiu.micro.blog.application.model.DeletedBlogEntry;
import wiki.chiu.micro.blog.application.model.DeletedBlogItem;
import wiki.chiu.micro.blog.application.model.DeletedBlogPage;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.model.SensitiveContentDraft;
import wiki.chiu.micro.blog.application.port.in.BlogService;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringStore;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.blog.domain.SensitiveContent;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public class BlogServiceImpl implements BlogService {

    private static final int RECYCLE_RETENTION_DAYS = 7;

    private final BlogQueryStore blogs;

    private final BlogRuntimeStore runtimeStore;

    private final BlogWriter blogWrapper;

    private final BlogSearchGateway blogSearch;

    private final BlogAccessPolicy accessPolicy;

    private final BlogAuthoringStore authoring;

    public BlogServiceImpl(
        BlogQueryStore blogs,
        BlogRuntimeStore runtimeStore,
        BlogWriter blogWrapper,
        BlogSearchGateway blogSearch,
        BlogAccessPolicy accessPolicy,
        BlogAuthoringStore authoring) {
        this.blogs = blogs;
        this.runtimeStore = runtimeStore;
        this.blogWrapper = blogWrapper;
        this.blogSearch = blogSearch;
        this.accessPolicy = accessPolicy;
        this.authoring = authoring;
    }

    @Override
    public BlogEdit findEdit(Long id, Long userId, List<DataPermissionEnum> dataPermissions) {
        if (id == null) {
            Blog blank = Blog.blankFor(userId);
            return edit(blank, null, null, null, List.of(), accessPolicy.permissions(
                blank, userId, dataPermissions));
        }
        BlogIdentity identity =
            authoring.findIdentity(id).orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        Blog published = blogs.findById(id).orElse(null);
        BlogWorkingDraft draft = authoring.findDraft(id).orElse(null);
        Blog permissionBlog =
            published == null ? draftBlog(identity, draft) : published;
        if (!accessPolicy.canEdit(permissionBlog, userId, dataPermissions)) {
            throw new MissException(ExceptionMessage.EDIT_NO_AUTH.getMsg());
        }
        if (published != null) {
            accessPolicy.requireCollaboration(published, userId, dataPermissions);
        }
        boolean draftNewer = draft != null && draft.newerThan(identity);
        if (draftNewer) {
            return edit(
                new Blog(
                    identity.id(),
                    identity.userId(),
                    draft.title(),
                    draft.description(),
                    draft.content(),
                    identity.created(),
                    draft.updatedAt(),
                    BlogRenderMode.SENSITIVE_FILTER == draft.renderMode()
                        ? 2
                        : 0,
                    draft.link(),
                    identity.readCount(),
                    identity.publicationRevision()),
                draft,
                identity,
                null,
                toDrafts(draft.sensitiveContent()),
                accessPolicy.permissions(permissionBlog, userId, dataPermissions));
        }
        Blog selected = published == null ? draftBlog(identity, draft) : published;
        List<BlogSensitiveSpan> spans =
            draft != null && published == null
                ? draft.sensitiveContent()
                : published == null
                    ? List.of()
                    : blogs.findSensitiveByBlogId(id).stream()
                        .map(
                            span ->
                                new BlogSensitiveSpan(
                                    span.startIndex(), span.endIndex(), span.type()))
                        .toList();
        return edit(
            selected,
            draft,
            identity,
            published == null ? null : identity.publishedRevisionId(),
            spans,
            accessPolicy.permissions(permissionBlog, userId, dataPermissions));
    }

    @Override
    public BlogIdentity createDraftBlog(Long userId) {
        accessPolicy.requireAuthenticated(userId);
        return authoring.createIdentity(userId);
    }

    @Override
    public BlogWorkingDraft saveDraft(
        Long blogId,
        BlogDraft blog,
        Long userId,
        List<DataPermissionEnum> dataPermissions) {
        requireEditable(blogId, userId, dataPermissions);
        return authoring.saveDraft(
            blogId,
            blog.draftRevision(),
            command(blog),
            userId);
    }

    @Override
    public void deleteDraft(
        Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        requireEditable(blogId, userId, dataPermissions);
        authoring.deleteDraft(blogId);
    }

    @Override
    public BlogVersion saveDraftVersion(
        Long blogId,
        BlogDraft blog,
        Long userId,
        List<DataPermissionEnum> dataPermissions) {
        requireEditable(blogId, userId, dataPermissions);
        return authoring.saveDraftVersion(
            blogId,
            blog.draftRevision(),
            command(blog),
            userId);
    }

    @Override
    public void publish(
        Long blogId,
        Long revisionId,
        Integer accessMode,
        Long userId,
        List<DataPermissionEnum> dataPermissions) {
        requireEditable(blogId, userId, dataPermissions);
        authoring.publish(blogId, revisionId, userId, accessMode);
    }

    @Override
    public void offline(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        requireEditable(blogId, userId, dataPermissions);
        authoring.offline(blogId, userId);
    }

    @Override
    public void online(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        requireEditable(blogId, userId, dataPermissions);
        authoring.online(blogId, userId);
    }

    @Override
    public BlogVersion findVersion(
        Long blogId, Long revisionId, Long userId, List<DataPermissionEnum> dataPermissions) {
        requireEditable(blogId, userId, dataPermissions);
        return authoring.findVersion(blogId, revisionId)
            .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
    }

    @Override
    public BlogVersionComparison compareVersion(
        Long blogId, Long revisionId, Long userId, List<DataPermissionEnum> dataPermissions) {
        BlogVersion current = findVersion(blogId, revisionId, userId, dataPermissions);
        BlogVersion previous =
            authoring.findPreviousVersion(blogId, revisionId)
                .orElseThrow(() -> new MissException("previous revision not found"));
        return new BlogVersionComparison(previous, current);
    }

    @Override
    public Page<BlogVersionSummary> findVersions(
        Long blogId,
        Integer page,
        Integer pageSize,
        String keywords,
        Long userId,
        List<DataPermissionEnum> dataPermissions) {
        requireEditable(blogId, userId, dataPermissions);
        return blogSearch.searchVersions(
            blogId,
            page,
            pageSize,
            keywords,
            userId,
            dataPermissions.contains(DataPermissionEnum.BLOG_VIEW_ALL));
    }

    private void requireEditable(
        Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        BlogIdentity identity =
            authoring.findIdentity(blogId).orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        Blog blog =
            blogs.findById(blogId)
                .orElseGet(
                    () ->
                        new Blog(
                            identity.id(),
                            identity.userId(),
                            "",
                            "",
                            "",
                            identity.created(),
                            identity.created(),
                            BlogStatusEnum.DRAFT.getCode(),
                            "",
                            identity.readCount(),
                            identity.publicationRevision()));
        accessPolicy.requireEdit(blog, userId, dataPermissions);
    }

    private static BlogContentCommand command(BlogDraft blog) {
        BlogRenderMode renderMode =
            blog.status() != null
                    && blog.status()
                        == BlogStatusEnum.SENSITIVE_FILTER.getCode()
                ? BlogRenderMode.SENSITIVE_FILTER
                : BlogRenderMode.NORMAL;
        return new BlogContentCommand(
            blog.title(),
            blog.description(),
            blog.content(),
            blog.link(),
            renderMode,
            blog.sensitiveContentList().stream()
                .map(
                    span ->
                        new BlogSensitiveSpan(
                            span.startIndex(), span.endIndex(), span.type()))
                .toList());
    }

    private static Blog draftBlog(BlogIdentity identity, BlogWorkingDraft draft) {
        if (draft == null) {
            return new Blog(
                identity.id(),
                identity.userId(),
                "",
                "",
                "",
                identity.created(),
                identity.created(),
                BlogStatusEnum.DRAFT.getCode(),
                "",
                identity.readCount(),
                identity.publicationRevision());
        }
        return new Blog(
            identity.id(),
            identity.userId(),
            draft.title(),
            draft.description(),
            draft.content(),
            identity.created(),
            draft.updatedAt(),
            draft.renderMode() == BlogRenderMode.SENSITIVE_FILTER
                ? BlogStatusEnum.SENSITIVE_FILTER.getCode()
                : BlogStatusEnum.DRAFT.getCode(),
            draft.link(),
            identity.readCount(),
            identity.publicationRevision());
    }

    private static BlogEdit edit(
        Blog blog,
        BlogWorkingDraft draft,
        BlogIdentity identity,
        Long publishedRevisionId,
        List<BlogSensitiveSpan> spans,
        BlogPermissions permissions) {
        boolean draftNewer = draft != null && identity != null && draft.newerThan(identity);
        return new BlogEdit(
            blog.id(),
            blog.userId(),
            blog.title(),
            blog.description(),
            blog.link(),
            blog.content(),
            blog.status(),
            draft == null ? null : draft.draftRevision(),
            draft == null ? null : draft.basePublishedRevisionId(),
            publishedRevisionId,
            draft != null,
            draftNewer,
            spans.stream()
                .map(
                    span ->
                        new SensitiveContentDraft(
                            span.type(), span.startIndex(), span.endIndex()))
                .toList(),
            permissions);
    }

    private static List<BlogSensitiveSpan> toDrafts(
        List<BlogSensitiveSpan> sensitive) {
        return sensitive;
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
        Page<BlogListItem> result = blogSearch.searchAdmin(searchQuery);
        List<Long> ids = result.content().stream().map(BlogListItem::id).toList();
        Map<Long, Integer> recent = ids.isEmpty() ? Map.of() : runtimeStore.readCounts(ids);
        return result.withContent(
            result.content().stream()
                .map(
                    item ->
                        new BlogListItem(
                            item.id(),
                            item.title(),
                            item.description(),
                            item.content(),
                            item.link(),
                            item.readCount(),
                            recent.getOrDefault(item.id(), 0),
                            item.created(),
                            item.updated(),
                            item.status(),
                            item.hasDraft(),
                            item.draftNewer()))
                .toList());
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
