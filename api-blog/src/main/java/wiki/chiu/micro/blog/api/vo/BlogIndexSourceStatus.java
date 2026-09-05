package wiki.chiu.micro.blog.api.vo;

public record BlogIndexSourceStatus(boolean readOnly, long readyEvents, long pausedEvents, long total) {

    public boolean ready() {
        return readOnly && readyEvents == 0 && pausedEvents == 0;
    }
}
