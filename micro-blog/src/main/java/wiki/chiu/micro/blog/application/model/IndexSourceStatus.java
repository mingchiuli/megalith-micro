package wiki.chiu.micro.blog.application.model;

/**
 * Whether the blog table may be used as a search index source right now.
 */
public record IndexSourceStatus(boolean readOnly, long readyEvents, long pausedEvents, long total) {

    public boolean ready() {
        return readOnly && readyEvents == 0 && pausedEvents == 0;
    }
}
