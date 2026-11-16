package wiki.chiu.micro.exhibit.application.port.out;

import java.util.List;

public interface ExhibitMetrics {

    boolean consumeReadToken(Long blogId, String token);

    List<Long> visitCounts();

}
