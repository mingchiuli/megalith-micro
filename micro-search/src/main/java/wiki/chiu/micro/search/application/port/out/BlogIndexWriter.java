package wiki.chiu.micro.search.application.port.out;

import java.util.List;

import wiki.chiu.micro.search.application.model.BlogAdminIndexChange;
import wiki.chiu.micro.search.application.model.BlogIndexChange;

public interface BlogIndexWriter {

    void write(BlogIndexChange change);

    void writeAdmin(BlogAdminIndexChange change);

    void writeRevision(BlogAdminIndexChange revision);

    default void writeAll(List<BlogIndexChange> changes) {
        changes.forEach(this::write);
    }

    default void writeAdminAll(List<BlogAdminIndexChange> changes) {
        changes.forEach(this::writeAdmin);
    }

    default void writeRevisionAll(List<BlogAdminIndexChange> revisions) {
        revisions.forEach(this::writeRevision);
    }
}
