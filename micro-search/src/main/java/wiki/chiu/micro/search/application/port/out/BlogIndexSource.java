package wiki.chiu.micro.search.application.port.out;

import java.util.List;

import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.IndexSourceStatus;
import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogRevisionIndexEntry;

public interface BlogIndexSource {

    IndexSourceStatus status();

    List<BlogIndexChange> snapshots(long afterId, int limit);

    List<BlogAdminIndexChange> adminSnapshots(long afterId, int limit);

    List<BlogRevisionIndexEntry> revisionSnapshots(long afterRevisionId, int limit);
}
