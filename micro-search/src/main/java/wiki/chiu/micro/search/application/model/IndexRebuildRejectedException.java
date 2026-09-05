package wiki.chiu.micro.search.application.model;

public class IndexRebuildRejectedException extends RuntimeException {

    public IndexRebuildRejectedException(String message) {
        super(message);
    }
}
