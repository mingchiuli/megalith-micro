package wiki.chiu.micro.exhibit.application.port.out;

import java.util.List;

import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.BlogHotRead;
import wiki.chiu.micro.exhibit.application.model.RecommendationFeed;

/**
 * Reads the blog content this service exhibits, already composed for display.
 */
public interface BlogReader {

    BlogExhibit findById(Long id);

    void incrementViews(Long id);

    List<BlogHotRead> hotBlogs(int limit);

    RecommendationFeed recommendationFeed(String cursor);
}
