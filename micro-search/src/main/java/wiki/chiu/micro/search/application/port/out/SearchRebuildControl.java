package wiki.chiu.micro.search.application.port.out;

import java.util.function.Supplier;

public interface SearchRebuildControl {

    <T> T runExclusive(Supplier<T> task);

    void requireQuiescent();
}
