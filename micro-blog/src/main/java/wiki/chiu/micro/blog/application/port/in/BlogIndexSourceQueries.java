package wiki.chiu.micro.blog.application.port.in;

import java.util.List;

import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.common.lang.BlogSnapshot;

public interface BlogIndexSourceQueries {

    BlogIndexSourceStatus status();

    List<BlogSnapshot> snapshots(long afterId, int limit);
}
