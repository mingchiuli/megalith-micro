package wiki.chiu.micro.exhibit.application.service;

import wiki.chiu.micro.common.enums.BlogOperateEnum;
import wiki.chiu.micro.exhibit.application.model.BlogChange;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;
import wiki.chiu.micro.exhibit.application.port.out.BlogExistenceStore;

public final class CreateBlogChangeMaintenanceHandler extends AbstractBlogChangeMaintenanceHandler {

    private final BlogExistenceStore existence;

    public CreateBlogChangeMaintenanceHandler(
        BlogEventRevisionGuard revisionGuard,
        BlogExistenceStore existence) {
        super(revisionGuard);
        this.existence = existence;
    }

    @Override
    public BlogOperateEnum operation() {
        return BlogOperateEnum.CREATE;
    }

    @Override
    protected void applyChangeUnderGuard(BlogChange change) {
        Long blogId = change.blogId();
        existence.markPresent(blogId);
    }
}
