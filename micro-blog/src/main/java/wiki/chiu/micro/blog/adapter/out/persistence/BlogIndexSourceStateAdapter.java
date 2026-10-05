package wiki.chiu.micro.blog.adapter.out.persistence;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.application.model.IndexSourceStatus;
import wiki.chiu.micro.blog.application.port.out.BlogIndexSourceState;
import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.common.outbox.application.OutboxStore;
import wiki.chiu.micro.common.outbox.domain.OutboxProducer;

@Component
public class BlogIndexSourceStateAdapter implements BlogIndexSourceState {

    private final BlogMaintenanceMode maintenance;
    private final BlogRepository blogs;
    private final OutboxStore outbox;

    public BlogIndexSourceStateAdapter(
        BlogMaintenanceMode maintenance, BlogRepository blogs, OutboxStore outbox) {
        this.maintenance = maintenance;
        this.blogs = blogs;
        this.outbox = outbox;
    }

    @Override
    public IndexSourceStatus status() {
        var status = outbox.status(OutboxProducer.BLOG);
        return new IndexSourceStatus(
            maintenance.readOnly(), status.ready(), status.paused(), blogs.count());
    }
}
