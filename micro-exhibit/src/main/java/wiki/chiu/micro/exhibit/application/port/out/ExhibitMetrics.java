package wiki.chiu.micro.exhibit.application.port.out;

import java.util.List;
import wiki.chiu.micro.exhibit.application.model.BlogScore;

public interface ExhibitMetrics {

  boolean consumeReadToken(Long blogId, String token);

  List<Long> visitCounts();

  List<BlogScore> topReadBlogs(int limit);
}
