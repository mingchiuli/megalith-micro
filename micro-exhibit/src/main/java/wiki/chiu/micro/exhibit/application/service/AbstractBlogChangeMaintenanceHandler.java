package wiki.chiu.micro.exhibit.application.service;

import wiki.chiu.micro.exhibit.application.model.BlogChange;
import wiki.chiu.micro.exhibit.application.port.out.BlogEventRevisionGuard;

/**
 * Shared plumbing for the per-operation maintenance strategies: every change runs only when the event
 * survived the per-blog revision guard.
 */
abstract sealed class AbstractBlogChangeMaintenanceHandler implements BlogChangeMaintenanceHandler
    permits CreateBlogChangeMaintenanceHandler,
        DeleteBlogChangeMaintenanceHandler,
        UpdateBlogChangeMaintenanceHandler {

    private final BlogEventRevisionGuard revisionGuard;

    protected AbstractBlogChangeMaintenanceHandler(
        BlogEventRevisionGuard revisionGuard) {
        this.revisionGuard = revisionGuard;
    }

    @Override
    public final void applyChange(BlogChange change) {
        revisionGuard.applyIfNewer(
            change.blogId(),
            change.revision(),
            () -> applyChangeUnderGuard(change));
    }

    protected abstract void applyChangeUnderGuard(BlogChange change);
}
