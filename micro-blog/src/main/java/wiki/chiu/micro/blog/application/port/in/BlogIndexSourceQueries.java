package wiki.chiu.micro.blog.application.port.in;

import java.util.List;

import wiki.chiu.micro.blog.application.model.IndexSourceStatus;
import wiki.chiu.micro.common.model.BlogSnapshot;

public interface BlogIndexSourceQueries {

    IndexSourceStatus status();

    List<BlogSnapshot> snapshots(long afterId, int limit);
}
