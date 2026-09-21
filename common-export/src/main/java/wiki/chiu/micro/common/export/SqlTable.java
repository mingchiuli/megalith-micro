package wiki.chiu.micro.common.export;

import java.util.List;

/**
 * Describes one exported table: its SQL name and the ordered columns to export.
 */
public record SqlTable<E>(String name, List<SqlColumn<E>> columns) {

    public SqlTable {
        columns = List.copyOf(columns);
    }

    @SafeVarargs
    public static <E> SqlTable<E> of(String name, SqlColumn<E>... columns) {
        return new SqlTable<>(name, List.of(columns));
    }
}
