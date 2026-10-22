package wiki.chiu.micro.search.application.port.out;

import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;

public interface BlogIndexWriter {

    void write(BlogIndexChange change);

    void writeAdmin(BlogAdminIndexChange change);
}
