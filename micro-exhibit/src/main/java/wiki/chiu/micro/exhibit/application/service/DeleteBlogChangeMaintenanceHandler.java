package wiki.chiu.micro.exhibit.application.service;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.exhibit.application.model.BlogChange;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;
import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;

public final class DeleteBlogChangeMaintenanceHandler extends AbstractBlogChangeMaintenanceHandler {

    private final BlogReadStateStore readStateStore;

    private final BlogExistenceStore existence;

    public DeleteBlogChangeMaintenanceHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogReadStateStore readStateStore,
        BlogExistenceStore existence) {
        super(revisionGuard);
        this.readStateStore = readStateStore;
        this.existence = existence;
    }

    @Override
    public BlogOperateEnum operation() {
        return BlogOperateEnum.REMOVE;
    }

    @Override
    protected void applyChangeUnderGuard(BlogChange change) {
        Long blogId = change.blogId();
        readStateStore.clearReadToken(blogId);
        existence.markAbsent(blogId);
        readStateStore.removeFromHotRead(blogId);
    }
}
