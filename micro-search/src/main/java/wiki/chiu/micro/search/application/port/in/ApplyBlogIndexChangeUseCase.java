package wiki.chiu.micro.search.application.port.in;

import wiki.chiu.micro.search.application.model.BlogIndexChange;

public interface ApplyBlogIndexChangeUseCase {

  void apply(BlogIndexChange change);
}
