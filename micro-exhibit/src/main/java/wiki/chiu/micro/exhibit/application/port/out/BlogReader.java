package wiki.chiu.micro.exhibit.application.port.out;

import java.util.List;

import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.BlogHotRead;
import wiki.chiu.micro.exhibit.application.model.BlogRecommendation;
import wiki.chiu.micro.exhibit.application.model.Page;

/**
 * Reads the blog content this service exhibits, already composed for display.
 */
public interface BlogReader {

    BlogExhibit findById(Long id);

    void incrementViews(Long id);

    List<BlogHotRead> hotBlogs(int limit);

    List<BlogRecommendation> relatedArticles(Long blogId, int limit);

    Page<BlogRecommendation> recommendationPage(int currentPage);
}
