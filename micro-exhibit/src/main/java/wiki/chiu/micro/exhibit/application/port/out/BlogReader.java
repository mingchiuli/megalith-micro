package wiki.chiu.micro.exhibit.application.port.out;

import wiki.chiu.micro.common.page.PageAdapter;
import wiki.chiu.micro.exhibit.dto.BlogDescriptionDto;
import wiki.chiu.micro.exhibit.dto.BlogExhibitDto;

public interface BlogReader {

  BlogExhibitDto findById(Long id);

  void incrementViews(Long id);

  PageAdapter<BlogDescriptionDto> findPage(Integer currentPage);
}
