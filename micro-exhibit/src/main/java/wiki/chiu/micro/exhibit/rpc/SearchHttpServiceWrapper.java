package wiki.chiu.micro.exhibit.rpc;

import org.springframework.stereotype.Component;
import wiki.chiu.micro.common.rpc.RemoteResult;
import wiki.chiu.micro.search.api.SearchHttpService;

/** BlogHttpServiceWrapper */
@Component
public class SearchHttpServiceWrapper {

  private final SearchHttpService searchHttpService;

  public SearchHttpServiceWrapper(SearchHttpService searchHttpService) {
    this.searchHttpService = searchHttpService;
  }

  public void addReadCount(Long blogId) {
    RemoteResult.requireSuccess(() -> searchHttpService.addReadCount(blogId));
  }
}
