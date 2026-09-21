package wiki.chiu.micro.common.export;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public final class SQLUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private SQLUtils() {
    }

    /**
     * Renders entities as a multi-row INSERT statement using the ordered column definition of the
     * given table. Values are formatted from their runtime type so exported statements stay
     * identical regardless of how the entity is mapped.
     */
    public static <E> String insertSql(List<E> entities, SqlTable<E> table) {
        if (entities == null || entities.isEmpty() || table.columns().isEmpty()) {
            return "";
        }

        String columns =
            table.columns().stream().map(SqlColumn::name).collect(Collectors.joining(", "));
        String prefix = "INSERT INTO " + table.name() + " (" + columns + ") VALUES ";

        return entities.stream()
            .map(entity -> "(" + formatRow(entity, table.columns()) + ")")
            .collect(Collectors.joining(",\n", prefix, ";"));
    }

    public static String compose(String... sqlList) {
        return String.join("\n", sqlList);
    }

    private static <E> String formatRow(E entity, List<SqlColumn<E>> columns) {
        return columns.stream()
            .map(column -> formatValue(column.value().apply(entity)))
            .collect(Collectors.joining(", "));
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        if (value instanceof Number number) {
            return number.toString();
        }
        if (value instanceof Boolean flag) {
            return flag ? "1" : "0";
        }
        if (value instanceof LocalDateTime localDateTime) {
            return "'" + localDateTime.format(DATE_TIME_FORMATTER) + "'";
        }
        if (value instanceof LocalDate localDate) {
            return "'" + localDate.format(DATE_FORMATTER) + "'";
        }
        if (value instanceof Date date) {
            return "'" + new java.sql.Timestamp(date.getTime()) + "'";
        }
        return "'" + escapeSqlString(String.valueOf(value)) + "'";
    }

    /**
     * 转义 SQL 字符串中的特殊字符
     */
    private static String escapeSqlString(String str) {
        return str.replace("\\", "\\\\") // 反斜杠
            .replace("'", "''") // 单引号（SQL 标准转义）
            .replace("\n", "\\n") // 换行符
            .replace("\r", "\\r") // 回车符
            .replace("\t", "\\t") // 制表符
            .replace("\0", "\\0"); // NULL 字符
    }
}
