package wiki.chiu.micro.search.application.port.out;

import java.util.List;

import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;

public interface BlogBootstrapIndexSource {

    List<BlogIndexChange> publicSnapshots(long afterId, int limit);

    List<BlogAdminIndexChange> adminSnapshots(long afterId, int limit);

    List<BlogAdminIndexChange> revisionSnapshots(long afterRevisionId, int limit);
}
