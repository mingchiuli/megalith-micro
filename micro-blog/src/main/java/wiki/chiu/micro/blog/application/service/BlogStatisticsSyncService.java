package wiki.chiu.micro.blog.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import wiki.chiu.micro.blog.application.model.BlogReadCount;
import wiki.chiu.micro.blog.application.port.in.BlogStatisticsSync;
import wiki.chiu.micro.blog.application.port.out.BlogQueryStore;
import wiki.chiu.micro.blog.application.port.out.BlogStatisticsGateway;

@Service
public class BlogStatisticsSyncService implements BlogStatisticsSync {

    private final BlogQueryStore blogs;
    private final BlogStatisticsGateway statistics;

    public BlogStatisticsSyncService(BlogQueryStore blogs, BlogStatisticsGateway statistics) {
        this.blogs = blogs;
        this.statistics = statistics;
    }

    @Override
    public long synchronize(int batchSize) {
        if (batchSize < 1 || batchSize > 1000) {
            throw new IllegalArgumentException("statistics batch size must be between 1 and 1000");
        }
        long afterId = 0;
        long processed = 0;
        while (true) {
            List<BlogReadCount> batch = blogs.findReadCountsAfter(afterId, batchSize);
            if (batch.isEmpty()) {
                return processed;
            }
            statistics.updateReadCounts(batch);
            processed += batch.size();
            afterId = batch.getLast().blogId();
            if (batch.size() < batchSize) {
                return processed;
            }
        }
    }
}
