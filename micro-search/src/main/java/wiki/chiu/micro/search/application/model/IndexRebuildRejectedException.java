package wiki.chiu.micro.search.application.model;

public class IndexRebuildRejectedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public IndexRebuildRejectedException(String message) {
        super(message);
    }
}
