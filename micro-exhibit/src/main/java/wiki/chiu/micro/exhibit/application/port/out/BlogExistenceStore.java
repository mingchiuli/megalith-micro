package wiki.chiu.micro.exhibit.application.port.out;

import java.util.List;
import java.util.Optional;

public interface BlogExistenceStore {

    State lookup(Long blogId);

    void markPresent(Long blogId);

    void markAbsent(Long blogId);

    Optional<Rebuild> tryBeginRebuild();

    enum State {
        PRESENT,
        ABSENT,
        UNKNOWN
    }

    interface Rebuild extends AutoCloseable {

        void addAll(List<Long> blogIds);

        void publish();

        @Override
        void close();
    }
}
