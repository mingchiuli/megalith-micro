package wiki.chiu.micro.search.application.port.out;

import java.util.List;

import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.IndexSourceStatus;

public interface BlogIndexSource {

    IndexSourceStatus status();

    List<BlogIndexChange> snapshots(long afterId, int limit);
}
