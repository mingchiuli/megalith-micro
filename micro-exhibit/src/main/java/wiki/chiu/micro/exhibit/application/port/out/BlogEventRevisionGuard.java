package wiki.chiu.micro.exhibit.application.port.out;

/**
 * Serializes blog change events per blog and drops the ones that arrive out of order, so a stale
 * event can never overwrite the effect of a newer one.
 */
public interface BlogEventRevisionGuard {

    /**
     * Runs {@code action} while holding the blog's event lock, but only when {@code revision} is
     * newer than the revision last applied to that blog.
     *
     * @param blogId the blog the event belongs to
     * @param revision the revision carried by the event
     * @param action the work to perform when the event is the newest one seen
     */
    void applyIfNewer(Long blogId, long revision, Runnable action);
}
