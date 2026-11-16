package wiki.chiu.micro.exhibit.application.service;

import static wiki.chiu.micro.common.enums.BlogStatusEnum.NORMAL;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.exhibit.application.model.BlogChange;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogReadStateStore;

public final class UpdateBlogChangeMaintenanceHandler extends AbstractBlogChangeMaintenanceHandler {

    private final BlogReadStateStore readStateStore;

    public UpdateBlogChangeMaintenanceHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogReadStateStore readStateStore) {
        super(revisionGuard);
        this.readStateStore = readStateStore;
    }

    @Override
    public BlogOperateEnum operation() {
        return BlogOperateEnum.UPDATE;
    }

    @Override
    protected void applyChangeUnderGuard(BlogChange change) {
        Long blogId = change.blogId();
        if (NORMAL.getCode().equals(change.status())) {
            readStateStore.clearReadToken(blogId);
        }
    }
}
