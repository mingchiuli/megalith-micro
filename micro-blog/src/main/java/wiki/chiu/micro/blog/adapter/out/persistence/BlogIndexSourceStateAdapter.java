package wiki.chiu.micro.blog.adapter.out.persistence;

import org.springframework.stereotype.Component;

import wiki.chiu.micro.blog.adapter.out.persistence.repository.BlogRepository;
import wiki.chiu.micro.blog.api.vo.BlogIndexSourceStatus;
import wiki.chiu.micro.blog.application.port.out.BlogIndexSourceState;
import wiki.chiu.micro.blog.config.BlogMaintenanceProperties;
import wiki.chiu.micro.outbox.OutboxProducer;
import wiki.chiu.micro.outbox.OutboxStore;

@Component
public class BlogIndexSourceStateAdapter implements BlogIndexSourceState {

    private final BlogMaintenanceProperties maintenance;
    private final BlogRepository blogs;
    private final OutboxStore outbox;

    public BlogIndexSourceStateAdapter(
        BlogMaintenanceProperties maintenance, BlogRepository blogs, OutboxStore outbox) {
        this.maintenance = maintenance;
        this.blogs = blogs;
        this.outbox = outbox;
    }

    @Override
    public BlogIndexSourceStatus status() {
        var status = outbox.status(OutboxProducer.BLOG);
        return new BlogIndexSourceStatus(maintenance.isReadOnly(), status.ready(), status.paused(), blogs.count());
    }
}
