package wiki.chiu.micro.blog.application.port.out;

import java.util.List;

import wiki.chiu.micro.blog.application.model.BlogReadCount;

public interface BlogStatisticsGateway {

    void updateReadCounts(List<BlogReadCount> counts);
}
