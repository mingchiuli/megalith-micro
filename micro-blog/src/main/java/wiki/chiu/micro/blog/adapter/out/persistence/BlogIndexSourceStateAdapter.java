package wiki.chiu.micro.blog.adapter.out.persistence;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRevisionRepository;
import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.blog.application.model.IndexSourceStatus;
import wiki.chiu.micro.blog.application.port.out.BlogIndexSourceState;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxAdministration;

@Component
public class BlogIndexSourceStateAdapter implements BlogIndexSourceState {

    private final BlogMaintenanceMode maintenance;
    private final BlogRepository blogs;
    private final BlogRevisionRepository revisions;
    private final OutboxAdministration outbox;

    public BlogIndexSourceStateAdapter(
        BlogMaintenanceMode maintenance, BlogRepository blogs, BlogRevisionRepository revisions,
        OutboxAdministration outbox) {
        this.maintenance = maintenance;
        this.blogs = blogs;
        this.revisions = revisions;
        this.outbox = outbox;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public boolean ready() {
        var status = outbox.status();
        return maintenance.readOnly() && status.ready() == 0 && status.paused() == 0;
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public IndexSourceStatus status() {
        var status = outbox.status();
        return new IndexSourceStatus(
            maintenance.readOnly(), status.ready(), status.paused(), blogs.count(),
            blogs.countByPublishedRevisionIdIsNotNull(), revisions.count());
    }
}
