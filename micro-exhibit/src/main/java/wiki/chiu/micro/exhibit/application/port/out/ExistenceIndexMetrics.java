package wiki.chiu.micro.exhibit.application.port.out;

/**
 * Reports the outcome of the blog existence index checks and rebuilds.
 */
public interface ExistenceIndexMetrics {

    void presentCheck();

    void absentCheck();

    void failOpenCheck();

    void rebuildSucceeded(long indexed);

    void rebuildFailed();

    void rebuildDurationNanos(long nanos);
}
