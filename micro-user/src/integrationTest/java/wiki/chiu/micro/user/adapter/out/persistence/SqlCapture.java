package wiki.chiu.micro.user.adapter.out.persistence;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * Captures the SQL Hibernate actually emits so integration tests can EXPLAIN the real statements.
 */
public class SqlCapture implements StatementInspector {

    private static final List<String> STATEMENTS = new CopyOnWriteArrayList<>();

    @Override
    public String inspect(String sql) {
        STATEMENTS.add(sql);
        return sql;
    }

    static void clear() {
        STATEMENTS.clear();
    }

    static String lastMatching(Predicate<String> predicate) {
        return STATEMENTS.stream()
            .filter(predicate)
            .reduce((first, second) -> second)
            .orElseThrow(
                () -> new AssertionError("no captured statement matched: " + STATEMENTS));
    }
}
