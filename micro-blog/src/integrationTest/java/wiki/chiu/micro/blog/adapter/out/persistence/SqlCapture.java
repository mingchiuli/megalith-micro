package wiki.chiu.micro.blog.adapter.out.persistence;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.hibernate.resource.jdbc.spi.StatementInspector;

/**
 * Captures Hibernate statements for database round-trip assertions.
 */
public class SqlCapture implements StatementInspector {

    static final ScopedValue<Consumer<String>> BEFORE_STATEMENT = ScopedValue.newInstance();

    private static final List<String> STATEMENTS = new CopyOnWriteArrayList<>();

    @Override
    public String inspect(String sql) {
        if (BEFORE_STATEMENT.isBound()) {
            BEFORE_STATEMENT.get().accept(sql);
        }
        STATEMENTS.add(sql);
        return sql;
    }

    static void clear() {
        STATEMENTS.clear();
    }

    static List<String> statements() {
        return List.copyOf(STATEMENTS);
    }

}
