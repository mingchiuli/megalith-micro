package wiki.chiu.micro.exhibit.application.model;

/**
 * A blog in the hot-read ranking, with the read count that placed it there.
 */
public record BlogHotRead(Long id, String title, Long readCount) {
}
