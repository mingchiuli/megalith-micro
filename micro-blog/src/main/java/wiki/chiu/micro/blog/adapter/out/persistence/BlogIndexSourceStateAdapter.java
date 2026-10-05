package wiki.chiu.micro.blog.adapter.out.persistence;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.application.model.IndexSourceStatus;
import wiki.chiu.micro.blog.application.port.out.BlogIndexSourceState;
import wiki.chiu.micro.blog.application.model.BlogMaintenanceMode;
import wiki.chiu.micro.common.outbox.application.port.in.OutboxAdministration;

@Component
public class BlogIndexSourceStateAdapter implements BlogIndexSourceState {

    private final BlogMaintenanceMode maintenance;
    private final BlogRepository blogs;
    private final OutboxAdministration outbox;

    public BlogIndexSourceStateAdapter(
        BlogMaintenanceMode maintenance, BlogRepository blogs, OutboxAdministration outbox) {
        this.maintenance = maintenance;
        this.blogs = blogs;
        this.outbox = outbox;
    }

    @Override
    public IndexSourceStatus status() {
        var status = outbox.status();
        return new IndexSourceStatus(
            maintenance.readOnly(), status.ready(), status.paused(), blogs.count());
    }
}
