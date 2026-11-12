package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.error.ExceptionMessage.NO_FOUND;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import wiki.chiu.micro.blog.application.model.BlogContentCommand;
import wiki.chiu.micro.blog.application.model.BlogDeletionTarget;
import wiki.chiu.micro.blog.application.model.BlogDraft;
import wiki.chiu.micro.blog.application.model.BlogDraftDeleteCommand;
import wiki.chiu.micro.blog.application.model.BlogDraftWriteCommand;
import wiki.chiu.micro.blog.application.model.BlogEdit;
import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.model.BlogListItem;
import wiki.chiu.micro.blog.application.model.BlogPublishCommand;
import wiki.chiu.micro.blog.application.model.BlogQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchQuery;
import wiki.chiu.micro.blog.application.model.BlogSearchSelection;
import wiki.chiu.micro.blog.application.model.BlogStatusWriteCommand;
import wiki.chiu.micro.blog.application.model.BlogVersionComparison;
import wiki.chiu.micro.blog.application.model.BlogVersionSummary;
import wiki.chiu.micro.blog.application.model.BlogVersionWriteCommand;
import wiki.chiu.micro.blog.application.model.DeletedBlogEntry;
import wiki.chiu.micro.blog.application.model.DeletedBlogItem;
import wiki.chiu.micro.blog.application.model.DeletedBlogPage;
import wiki.chiu.micro.blog.application.model.Page;
import wiki.chiu.micro.blog.application.model.SavedBlogVersion;
import wiki.chiu.micro.blog.application.port.in.BlogService;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringReader;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringWriter;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogVersion;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.common.enums.BlogRenderMode;
import wiki.chiu.micro.common.enums.BlogRevisionOrigin;
import wiki.chiu.micro.common.enums.BlogStatusEnum;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.error.CommonErrorCode;
import wiki.chiu.micro.common.exception.BaseException;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

public class BlogServiceImpl implements BlogService {

    private static final int RECYCLE_RETENTION_DAYS = 7;

    private final BlogQueryStore blogs;

    private final BlogRuntimeStore runtimeStore;

    private final BlogWriter blogWrapper;

    private final BlogSearchGateway blogSearch;

    private final BlogAccessPolicy accessPolicy;

    private final BlogAuthoringReader authoring;

    private final BlogAuthoringWriter authoringWriter;

    public BlogServiceImpl(
        BlogQueryStore blogs,
        BlogRuntimeStore runtimeStore,
        BlogWriter blogWrapper,
        BlogSearchGateway blogSearch,
        BlogAccessPolicy accessPolicy,
        BlogAuthoringReader authoring,
        BlogAuthoringWriter authoringWriter) {
        this.blogs = blogs;
        this.runtimeStore = runtimeStore;
        this.blogWrapper = blogWrapper;
        this.blogSearch = blogSearch;
        this.accessPolicy = accessPolicy;
        this.authoring = authoring;
        this.authoringWriter = authoringWriter;
    }

    @Override
    public BlogEdit findEdit(Long id, Long userId, List<DataPermissionEnum> dataPermissions) {
        return new BlogEditor(blogs, authoring, accessPolicy).loadEdit(id, userId, dataPermissions, false);
    }

    @Override
    public BlogIdentity createDraftBlog(Long userId) {
        accessPolicy.requireAuthenticated(userId);
        return authoringWriter.createIdentity(userId);
    }

    @Override
    public BlogWorkingDraft saveDraft(
        Long blogId,
        BlogDraft blog,
        Long userId,
        List<DataPermissionEnum> dataPermissions) {
        PreparedBlog prepared = prepareEditable(blogId, userId, dataPermissions);
        requireEpoch(prepared.identity(), blog.collaborationEpoch());
        requireDraftRevision(blogId, blog.draftRevision(), prepared.draft());
        return authoringWriter.saveDraft(
            new BlogDraftWriteCommand(
                prepared.identity(),
                prepared.draft(),
                command(blog),
                prepared.publishedRevision(),
                userId,
                LocalDateTime.now()));
    }

    @Override
    public void deleteDraft(
        Long blogId, Long userId, List<DataPermissionEnum> dataPermissions, String collaborationEpoch) {
        PreparedBlog prepared = prepareEditable(blogId, userId, dataPermissions);
        requireEpoch(prepared.identity(), collaborationEpoch);
        authoringWriter.deleteDraft(
            new BlogDraftDeleteCommand(
                prepared.identity(), prepared.draft(), prepared.publishedRevision()));
    }

    @Override
    public SavedBlogVersion saveDraftVersion(
        Long blogId,
        BlogDraft blog,
        Long userId,
        List<DataPermissionEnum> dataPermissions) {
        PreparedBlog prepared = prepareEditable(blogId, userId, dataPermissions);
        requireEpoch(prepared.identity(), blog.collaborationEpoch());
        requireDraftRevision(blogId, blog.draftRevision(), prepared.draft());
        long nextVersion =
            authoring.findLatestVersion(blogId)
                .map(version -> version.versionNo() + 1)
                .orElse(1L);
        return authoringWriter.saveDraftVersion(
            new BlogVersionWriteCommand(
                prepared.identity(),
                prepared.draft(),
                command(blog),
                prepared.publishedRevision(),
                nextVersion,
                userId,
                LocalDateTime.now()));
    }

    @Override
    public void publish(
        Long blogId,
        Long revisionId,
        Integer accessMode,
        Long userId,
        List<DataPermissionEnum> dataPermissions) {
        PreparedBlog prepared = prepareEditable(blogId, userId, dataPermissions);
        BlogVersion selected =
            authoring.findVersion(blogId, revisionId)
                .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        // Publishing another version can archive an unfinished working draft.
        requireNotBlank(selected.title(), "title");
        requireNotBlank(selected.description(), "description");
        requireNotBlank(selected.content(), "content");
        BlogVersion latest =
            authoring.findLatestVersion(blogId)
                .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        if (selected.firstPublishedAt() == null
            && selected.origin() == BlogRevisionOrigin.DRAFT
            && !selected.versionNo().equals(latest.versionNo())) {
            throw conflict(blogId);
        }
        BlogWorkingDraft draft = prepared.draft();
        BlogWorkingDraft archived =
            draft != null && !same(draft, selected) ? draft : null;
        authoringWriter.publish(
            new BlogPublishCommand(
                prepared.identity(),
                selected,
                draft,
                archived,
                latest.versionNo() + 1,
                accessMode,
                userId,
                LocalDateTime.now()));
    }

    @Override
    public void offline(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        changeStatus(blogId, userId, dataPermissions, false);
    }

    @Override
    public void online(Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        changeStatus(blogId, userId, dataPermissions, true);
    }

    @Override
    public BlogVersion findVersion(
        Long blogId, Long revisionId, Long userId, List<DataPermissionEnum> dataPermissions) {
        prepareEditable(blogId, userId, dataPermissions);
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
        prepareEditable(blogId, userId, dataPermissions);
        return blogSearch.searchVersions(
            blogId,
            page,
            pageSize,
            keywords,
            userId,
            dataPermissions.contains(DataPermissionEnum.BLOG_VIEW_ALL));
    }

    private PreparedBlog prepareEditable(
        Long blogId, Long userId, List<DataPermissionEnum> dataPermissions) {
        BlogIdentity identity =
            authoring.findIdentity(blogId).orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        accessPolicy.requireEdit(identity, userId, dataPermissions);
        return new PreparedBlog(
            identity,
            authoring.findDraft(blogId).orElse(null),
            authoring.findPublishedVersion(blogId).orElse(null));
    }

    private void changeStatus(
        Long blogId, Long userId, List<DataPermissionEnum> dataPermissions, boolean online) {
        PreparedBlog prepared = prepareEditable(blogId, userId, dataPermissions);
        BlogVersion published =
            prepared.publishedRevision() == null
                ? authoring.findPublishedVersion(blogId)
                    .orElseThrow(() -> new MissException(NO_FOUND.getMsg()))
                : prepared.publishedRevision();
        authoringWriter.changeStatus(
            new BlogStatusWriteCommand(
                prepared.identity(),
                published,
                prepared.draft(),
                userId,
                LocalDateTime.now(),
                online));
    }

    private static boolean same(BlogWorkingDraft draft, BlogVersion revision) {
        return Objects.equals(draft.title(), revision.title())
            && Objects.equals(draft.description(), revision.description())
            && Objects.equals(draft.content(), revision.content())
            && Objects.equals(draft.link(), revision.link())
            && draft.renderMode() == revision.renderMode()
            && new HashSet<>(draft.sensitiveContent())
                .equals(new HashSet<>(revision.sensitiveContent()));
    }

    private static void requireNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private static void requireDraftRevision(
        Long blogId, Long expected, BlogWorkingDraft current) {
        if (expected != null
            && (current == null || !Objects.equals(expected, current.draftRevision()))) {
            throw conflict(blogId);
        }
    }

    private static void requireEpoch(BlogIdentity identity, String epoch) {
        if (!Objects.equals(identity.collaborationEpoch(), epoch)) {
            throw conflict(identity.id());
        }
    }

    private static BaseException conflict(Long blogId) {
        return new BaseException(
            CommonErrorCode.CONFLICT,
            "blog revision conflict: " + blogId);
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
        List<BlogDeletionTarget> deleted =
            blogs.findDeletionTargetsByIds(ids).stream()
                .filter(target -> accessPolicy.canDelete(target.blog(), userId, dataPermissions))
                .toList();
        deletePrepared(deleted, userId);
    }

    @Override
    public void deleteByUserIds(List<Long> userIds) {
        deletePrepared(blogs.findDeletionTargetsByUserIds(userIds), null);
    }

    private void deletePrepared(List<BlogDeletionTarget> deleted, Long operatorUserId) {
        if (deleted.isEmpty()) {
            return;
        }
        blogWrapper.deleteByIds(
            deleted,
            new BlogEventContext(
                BlogOperateEnum.REMOVE, operatorUserId));
    }

    private record PreparedBlog(
        BlogIdentity identity,
        BlogWorkingDraft draft,
        BlogVersion publishedRevision) {
    }

}
