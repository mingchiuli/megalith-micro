package wiki.chiu.micro.search.application.port.out;

import java.util.List;

import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;
import wiki.chiu.micro.search.application.model.BlogReadCount;

public interface BlogIndexWriter {

    void write(BlogIndexChange change);

    void writeAdmin(BlogAdminIndexChange change);

    void updateReadCounts(List<BlogReadCount> counts);

}
