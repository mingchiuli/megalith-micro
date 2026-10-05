package wiki.chiu.micro.exhibit.application.port.out;

/**
 * Owns the per-blog read state this service keeps outside the blog service: the one-time read token
 * of a locked blog and its place in the hot-read ranking.
 */
public interface BlogReadStateStore {

    /**
     * Forgets a blog's outstanding read token, so a token bought for an older revision stops
     * working.
     */
    void clearReadToken(Long blogId);

    /**
     * Drops a blog from the hot-read ranking.
     */
    void removeFromHotRead(Long blogId);
}
