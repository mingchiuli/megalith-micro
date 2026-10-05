package wiki.chiu.micro.exhibit.application.port.out;

import java.util.List;

import wiki.chiu.micro.exhibit.domain.SensitiveSpan;

/**
 * Reads the spans of a blog that the blog service flagged as sensitive.
 */
public interface SensitiveContentReader {

    List<SensitiveSpan> findSensitiveSpans(Long blogId);
}
