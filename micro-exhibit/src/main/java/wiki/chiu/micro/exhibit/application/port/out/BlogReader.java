package wiki.chiu.micro.exhibit.application.port.out;

import wiki.chiu.micro.exhibit.application.model.BlogDescription;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;
import wiki.chiu.micro.exhibit.application.model.Page;

/**
 * Reads the blog content this service exhibits, already composed for display.
 */
public interface BlogReader {

    BlogExhibit findById(Long id);

    void incrementViews(Long id);

    Page<BlogDescription> findPage(Integer currentPage);
}
