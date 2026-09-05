package wiki.chiu.micro.search.application.model;

public record IndexSourceStatus(boolean readOnly, long readyEvents, long pausedEvents, long total) {

    public boolean ready() {
        return readOnly && readyEvents == 0 && pausedEvents == 0;
    }
}
