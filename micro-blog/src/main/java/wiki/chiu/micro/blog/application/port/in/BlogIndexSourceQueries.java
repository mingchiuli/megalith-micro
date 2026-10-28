package wiki.chiu.micro.blog.application.port.in;

import java.util.List;

import wiki.chiu.micro.blog.application.model.IndexSourceStatus;
import wiki.chiu.micro.common.model.BlogAdminIndexSnapshot;
import wiki.chiu.micro.common.model.BlogRevisionSnapshot;
import wiki.chiu.micro.common.model.BlogSnapshot;

public interface BlogIndexSourceQueries {

    IndexSourceStatus status();

    List<BlogSnapshot> snapshots(long afterId, int limit);

    List<BlogSnapshot> bootstrapSnapshots(long afterId, int limit);

    List<BlogAdminIndexSnapshot> adminSnapshots(long afterId, int limit);

    List<BlogRevisionSnapshot> revisionSnapshots(long afterRevisionId, int limit);
}
