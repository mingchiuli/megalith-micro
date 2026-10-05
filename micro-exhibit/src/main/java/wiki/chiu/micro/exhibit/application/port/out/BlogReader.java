package wiki.chiu.micro.exhibit.application.port.out;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.application.model.BlogDescription;
import wiki.chiu.micro.exhibit.application.model.BlogExhibit;

/**
 * Reads the blog content this service exhibits, already composed for display.
 */
public interface BlogReader {

    BlogExhibit findById(Long id);

    void incrementViews(Long id);

    PageAdapter<BlogDescription> findPage(Integer currentPage);
}
