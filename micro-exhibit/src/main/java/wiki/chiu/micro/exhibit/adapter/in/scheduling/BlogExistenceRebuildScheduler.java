package wiki.chiu.micro.exhibit.adapter.in.scheduling;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import wiki.chiu.micro.exhibit.application.port.in.BlogExistenceService;

@Component
public class BlogExistenceRebuildScheduler {

    private final BlogExistenceService blogExistenceService;

    public BlogExistenceRebuildScheduler(BlogExistenceService blogExistenceService) {
        this.blogExistenceService = blogExistenceService;
    }

    @Scheduled(
        initialDelayString = "${megalith.blog.existence-index.initial-delay:0s}",
        fixedDelayString = "${megalith.blog.existence-index.reconcile-delay:30s}")
    public void rebuildIfRequired() {
        blogExistenceService.rebuildIfRequired();
    }
}
