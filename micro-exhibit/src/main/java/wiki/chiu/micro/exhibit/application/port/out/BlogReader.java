package wiki.chiu.micro.exhibit.application.port.out;

import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.RecommendationFeed;

/**
 * Reads the blog content this service exhibits, already composed for display.
 */
public interface BlogReader {

    BlogExhibit findById(Long id);

    void incrementViews(Long id);

    RecommendationFeed recommendationFeed(String cursor);
}
