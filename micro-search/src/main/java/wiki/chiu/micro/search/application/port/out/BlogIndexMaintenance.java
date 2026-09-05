package wiki.chiu.micro.search.application.port.out;

import java.util.List;

import wiki.chiu.micro.search.application.model.BlogIndexChange;

public interface BlogIndexMaintenance {

    String currentIndex();

    String createIndex();

    void writeSnapshots(String index, List<BlogIndexChange> snapshots);

    long refreshAndCount(String index);

    void activate(String expectedPreviousIndex, String index);
}
