package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.lang.ExceptionMessage.*;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.blog.application.model.BlogEventContext;
import wiki.chiu.micro.blog.application.model.DeletedBlogPage;
import wiki.chiu.micro.blog.application.port.in.BlogService;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogRuntimeStore;
import wiki.chiu.micro.blog.application.port.out.BlogSearchGateway;
import wiki.chiu.micro.blog.application.port.out.BlogWriter;
import wiki.chiu.micro.blog.convertor.*;
import wiki.chiu.micro.blog.domain.BlogEntity;
import wiki.chiu.micro.blog.domain.BlogSensitiveContentEntity;
import wiki.chiu.micro.blog.req.BlogEntityReq;
import wiki.chiu.micro.blog.req.BlogQueryReq;
import wiki.chiu.micro.blog.vo.BlogDeleteVo;
import wiki.chiu.micro.blog.vo.BlogEditVo;
import wiki.chiu.micro.blog.vo.BlogEntityVo;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.lang.*;
import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.search.api.req.BlogSysSearchReq;
import wiki.chiu.micro.search.api.vo.BlogSearchRpcVo;

@Service
public class BlogServiceImpl implements BlogService {

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
    public BlogEditVo findEdit(Long id, Long userId, List<DataPermissionEnum> dataPermissions) {

        BlogEntity blog;
        List<BlogEditVo.SensitiveContentVo> sensitiveContentList;
        if (id != null) {
            blog = blogs.findById(id).orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
            accessPolicy.requireCollaboration(blog, userId, dataPermissions);
            var sensitiveContentRpcList = blogs.findSensitiveByBlogId(id);
            sensitiveContentList = SensitiveContentVoConvertor.convert(sensitiveContentRpcList);
        } else {
            blog = createNewBlog(userId);
            sensitiveContentList = new ArrayList<>();
        }

        return BlogEditVoConvertor.convert(
            blog, sensitiveContentList, accessPolicy.permissions(blog, userId, dataPermissions));
    }

    private BlogEntity createNewBlog(Long userId) {
        return BlogEntity.builder()
            .userId(userId)
            .status(BlogStatusEnum.NORMAL.getCode())
            .content("")
            .description("")
            .link("")
            .title("")
            .build();
    }

    @Override
    public void saveOrUpdate(
        BlogEntityReq blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        BlogEntity current = getBlogEntity(blog, userId, dataPermissions);
        Long expectedRevision = blog.id().isPresent() ? current.getEventRevision() : null;
        BlogEntity candidate = BlogEntityConvertor.convert(blog, current);
        if (expectedRevision != null) {
            candidate.setUpdated(LocalDateTime.now());
        }

        List<Long> existingSensitiveIds =
            blog.id()
                .map(
                    blogId ->
                        blogs.findSensitiveByBlogId(blogId).stream()
                            .map(BlogSensitiveContentEntity::getId)
                            .toList())
                .orElseGet(List::of);
        List<BlogSensitiveContentEntity> blogSensitiveContentEntityList =
            blog.sensitiveContentList().stream()
                .distinct()
                .map(
                    item ->
                        BlogSensitiveContentEntity.builder()
                            .endIndex(item.endIndex())
                            .startIndex(item.startIndex())
                            .type(item.type())
                            .build())
                .toList();
        long totalCount = blogs.count() + (expectedRevision == null ? 1 : 0);
        Long newerOrSameCount =
            expectedRevision == null
                ? null
                : blogs.countCreatedSince(candidate.getCreated());
        BlogOperateEnum operation =
            expectedRevision == null ? BlogOperateEnum.CREATE : BlogOperateEnum.UPDATE;

        blogWrapper.saveOrUpdate(
            candidate,
            expectedRevision,
            existingSensitiveIds,
            blogSensitiveContentEntityList,
            new BlogEventContext(operation, userId, totalCount, newerOrSameCount));
    }

    private BlogEntity getBlogEntity(
        BlogEntityReq blog, Long userId, List<DataPermissionEnum> dataPermissions) {
        return blog.id()
            .map(
                blogId -> {
                    BlogEntity existing =
                        blogs
                            .findById(blogId)
                            .orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
                    accessPolicy.requireEdit(existing, userId, dataPermissions);
                    return existing;
                })
            .orElseGet(() -> BlogEntity.builder().userId(userId).readCount(0L).build());
    }

    @Override
    public PageAdapter<BlogEntityVo> findAllBlogs(
        BlogQueryReq blogQueryReq, Long userId, List<DataPermissionEnum> dataPermissions) {

        BlogSysSearchReq req =
            BlogSysSearchReqConvertor.convert(
                blogQueryReq, userId, dataPermissions.contains(DataPermissionEnum.BLOG_VIEW_ALL));
        BlogSearchRpcVo dto = blogSearch.searchBlogs(req);
        List<Long> ids = dto.ids();
        if (ids.isEmpty()) {
            return PageAdapter.emptyPage();
        }

        List<BlogEntity> items =
            blogs.findAllById(ids).stream()
                .sorted(Comparator.comparing(item -> ids.indexOf(item.getId())))
                .filter(item -> req.status() == null || Objects.equals(item.getStatus(), req.status()))
                .toList();

        List<BlogSensitiveContentEntity> blogSensitiveContentEntities =
            blogs.findSensitiveByBlogIds(ids);

        Map<Long, Integer> readMap = runtimeStore.readCounts(ids);

        return BlogEntityVoConvertor.convert(items, readMap, blogSensitiveContentEntities, dto);
    }

    @Override
    public PageAdapter<BlogDeleteVo> findDeletedBlogs(
        Integer currentPage, Integer size, Long userId) {
        DeletedBlogPage deleted =
            runtimeStore.deletedBlogs(userId, currentPage, size, LocalDateTime.now().minusDays(7));
        if (deleted.blogs().isEmpty()) {
            return PageAdapter.emptyPage();
        }
        return BlogDeleteVoConvertor.convert(
            deleted.expiredCount(), deleted.blogs(), currentPage, size, deleted.total());
    }

    @Override
    public void recoverDeletedBlog(Integer idx, Long userId) {
        var deleted = runtimeStore.deletedBlog(userId, idx);
        if (deleted.isEmpty()) {
            return;
        }
        BlogEntity recovered = deleted.orElseThrow().blog();
        blogWrapper.recoverDeletedBlog(
            recovered,
            new BlogEventContext(BlogOperateEnum.CREATE, userId, blogs.count() + 1, null));
        runtimeStore.removeDeletedBlog(userId, deleted.orElseThrow().receipt());
    }

    @Override
    public void deleteBatch(List<Long> ids, Long userId, List<DataPermissionEnum> dataPermissions) {
        List<BlogEntity> deleted =
            blogs.findAllById(ids).stream()
                .filter(blog -> accessPolicy.canDelete(blog, userId, dataPermissions))
                .toList();
        deletePrepared(deleted, userId);
    }

    @Override
    public void deleteByUserIds(List<Long> userIds) {
        deletePrepared(blogs.findByUserIds(userIds), null);
    }

    private void deletePrepared(List<BlogEntity> deleted, Long operatorUserId) {
        if (deleted.isEmpty()) {
            return;
        }
        deleted.forEach(blog -> blog.setEventRevision(blog.getEventRevision() + 1));
        List<Long> deletedIds = deleted.stream().map(BlogEntity::getId).toList();
        List<Long> sensitiveIds =
            blogs.findSensitiveByBlogIds(deletedIds).stream()
                .map(BlogSensitiveContentEntity::getId)
                .toList();
        long previousTotalCount = blogs.count();
        long totalCount = Math.max(0, previousTotalCount - deleted.size());

        blogWrapper.deleteByIds(
            deleted,
            sensitiveIds,
            new BlogEventContext(
                BlogOperateEnum.REMOVE,
                operatorUserId,
                totalCount,
                null,
                previousTotalCount));
    }
}
