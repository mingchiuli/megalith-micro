package wiki.chiu.micro.blog.application.service;

import static wiki.chiu.micro.common.error.ExceptionMessage.NO_FOUND;

import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogEdit;
import wiki.chiu.micro.blog.application.model.BlogEditorState;
import wiki.chiu.micro.blog.application.port.out.BlogAuthoringReader;
import wiki.chiu.micro.blog.domain.Blog;
import wiki.chiu.micro.blog.domain.BlogIdentity;
import wiki.chiu.micro.blog.domain.BlogWorkingDraft;
import wiki.chiu.micro.common.enums.DataPermissionEnum;
import wiki.chiu.micro.common.error.ExceptionMessage;
import wiki.chiu.micro.common.exception.MissException;
import wiki.chiu.micro.common.model.BlogSensitiveSpan;

/** Shared editor selection for HTTP readback and collaboration initialization. */
public final class BlogEditor {
    private final BlogAuthoringReader authoring;
    private final BlogAccessPolicy accessPolicy;

    public BlogEditor(BlogAuthoringReader authoring, BlogAccessPolicy accessPolicy) {
        this.authoring = authoring;
        this.accessPolicy = accessPolicy;
    }

    public BlogEdit loadEdit(Long id, Long userId, List<DataPermissionEnum> dataPermissions, boolean collaboration) {
        if (id == null) {
            Blog blank = Blog.blankFor(userId);
            return BlogModelConvertor.edit(blank, null, null, null, List.of(), accessPolicy.permissions(
                blank, userId, dataPermissions));
        }
        BlogEditorState state =
            authoring.findEditorState(id).orElseThrow(() -> new MissException(NO_FOUND.getMsg()));
        BlogIdentity identity = state.identity();
        Blog published = state.published();
        BlogWorkingDraft draft = state.draft();
        Blog permissionBlog =
            published == null ? BlogModelConvertor.draftBlog(identity, draft) : published;
        if (!(collaboration ? accessPolicy.canCollaborate(permissionBlog, userId, dataPermissions)
            : accessPolicy.canEdit(permissionBlog, userId, dataPermissions))) {
            throw new MissException(ExceptionMessage.EDIT_NO_AUTH.getMsg());
        }
        if (published != null) {
            accessPolicy.requireCollaboration(published, userId, dataPermissions);
        }
        boolean draftNewer = draft != null && draft.newerThan(identity);
        if (draftNewer) {
            return BlogModelConvertor.edit(
                BlogModelConvertor.toEditableDraft(identity, draft),
                draft,
                identity,
                null,
                draft.sensitiveContent(),
                accessPolicy.permissions(permissionBlog, userId, dataPermissions));
        }
        Blog selected = published == null ? BlogModelConvertor.draftBlog(identity, draft) : published;
        List<BlogSensitiveSpan> spans =
            draft != null && published == null
                ? draft.sensitiveContent()
                : published == null ? List.of() : published.sensitiveContent();
        return BlogModelConvertor.edit(
            selected,
            draft,
            identity,
            published == null ? null : identity.publishedRevisionId(),
            spans,
            accessPolicy.permissions(permissionBlog, userId, dataPermissions));
    }
}
